import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import '../styles/pages/AirlineInformation.css';

const AirlineInformation = () => {
  const [flights, setFlights] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedAirline, setSelectedAirline] = useState('ALL');
  const [selectedRoute, setSelectedRoute] = useState('ALL');
  const [searchTerm, setSearchTerm] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    loadFlights();
  }, []);

  const loadFlights = async () => {
    try {
      setLoading(true);
      const response = await axios.get('http://localhost:8080/api/flights');
      setFlights(response.data || []);
      setError(null);
    } catch (err) {
      console.error('Failed to load flights:', err);
      setError('Không thể tải danh sách chuyến bay');
    } finally {
      setLoading(false);
    }
  };

  // Get unique airlines
  const airlines = ['ALL', ...new Set(flights.map(f => f.airline))];
  
  // Get unique routes
  const routes = ['ALL', ...new Set(flights.map(f => `${f.origin} → ${f.destination}`))];

  // Filter flights
  const filteredFlights = flights.filter(flight => {
    // FIX: Loại bỏ chuyến bay đã hết thời gian (arriveTime < now)
    // Chỉ hiển thị chuyến bay chưa đến thời gian đến
    if (flight.arriveTime) {
      const arriveTime = new Date(flight.arriveTime);
      const now = new Date();
      if (arriveTime < now) {
        return false; // Chuyến bay đã hết thời gian, không hiển thị
      }
    }
    
    const matchesAirline = selectedAirline === 'ALL' || flight.airline === selectedAirline;
    const matchesRoute = selectedRoute === 'ALL' || `${flight.origin} → ${flight.destination}` === selectedRoute;
    const matchesSearch = searchTerm === '' || 
      flight.flightNumber.toLowerCase().includes(searchTerm.toLowerCase()) ||
      flight.airline.toLowerCase().includes(searchTerm.toLowerCase()) ||
      flight.origin.toLowerCase().includes(searchTerm.toLowerCase()) ||
      flight.destination.toLowerCase().includes(searchTerm.toLowerCase());
    
    return matchesAirline && matchesRoute && matchesSearch;
  });

  const formatDateTime = (dateString) => {
    return new Date(dateString).toLocaleString('vi-VN', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(amount || 0);
  };

  const getStatusBadge = (status) => {
    const statusConfig = {
      SCHEDULED: { color: '#3b82f6', bg: '#dbeafe', icon: '📅', label: 'Đã lên lịch' },
      BOARDING: { color: '#f59e0b', bg: '#fef3c7', icon: '🚪', label: 'Đang lên máy bay' },
      DEPARTED: { color: '#8b5cf6', bg: '#ede9fe', icon: '🛫', label: 'Đã cất cánh' },
      ARRIVED: { color: '#10b981', bg: '#d1fae5', icon: '🛬', label: 'Đã hạ cánh' },
      CANCELLED: { color: '#ef4444', bg: '#fee2e2', icon: '❌', label: 'Đã hủy' },
      DELAYED: { color: '#f97316', bg: '#ffedd5', icon: '⏰', label: 'Bị trễ' }
    };

    const config = statusConfig[status] || statusConfig.SCHEDULED;
    return (
      <span 
        className="status-badge" 
        style={{ color: config.color, background: config.bg }}
      >
        <span>{config.icon}</span>
        <span>{config.label}</span>
      </span>
    );
  };

  const handleBookFlight = (flight) => {
    // Navigate to seat selection with this flight
    navigate('/booking/seat-selection', { 
      state: { 
        preselectedFlight: flight,
        selectedFlight: flight,
        origin: flight.origin,
        destination: flight.destination,
        passengers: 1
      } 
    });
  };

  if (loading) {
    return (
      <div className="airline-info-page">
        <div className="loading-state">
          <div className="spinner"></div>
          <p>Đang tải thông tin chuyến bay...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="airline-info-page">
      {/* Hero Section */}
      <div className="hero-section">
        <div className="hero-content">
          <h1 className="hero-title">✈️ Thông Tin Chuyến Bay</h1>
          <p className="hero-subtitle">
            Xem thông tin chi tiết về tất cả các chuyến bay hiện có
          </p>
        </div>
      </div>

      <div className="container">
        {/* Stats */}
        <div className="stats-grid">
          <div className="stat-card">
            <div className="stat-icon">✈️</div>
            <div className="stat-content">
              <p className="stat-label">Tổng chuyến bay</p>
              <p className="stat-value">{flights.length}</p>
            </div>
          </div>
          
          <div className="stat-card">
            <div className="stat-icon">🏢</div>
            <div className="stat-content">
              <p className="stat-label">Hãng hàng không</p>
              <p className="stat-value">{airlines.length - 1}</p>
            </div>
          </div>
          
          <div className="stat-card">
            <div className="stat-icon">🌍</div>
            <div className="stat-content">
              <p className="stat-label">Tuyến bay</p>
              <p className="stat-value">{routes.length - 1}</p>
            </div>
          </div>
          
          <div className="stat-card">
            <div className="stat-icon">💺</div>
            <div className="stat-content">
              <p className="stat-label">Ghế khả dụng</p>
              <p className="stat-value">
                {flights.reduce((sum, f) => sum + (f.availableSeats || 0), 0)}
              </p>
            </div>
          </div>
        </div>

        {/* Filters */}
        <div className="filters-section">
          <div className="search-box">
            <span className="search-icon">🔍</span>
            <input
              type="text"
              placeholder="Tìm kiếm số hiệu, hãng bay, điểm đi/đến..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>

          <div className="filter-group">
            <label>Hãng bay:</label>
            <select 
              value={selectedAirline} 
              onChange={(e) => setSelectedAirline(e.target.value)}
            >
              {airlines.map(airline => (
                <option key={airline} value={airline}>
                  {airline === 'ALL' ? 'Tất cả' : airline}
                </option>
              ))}
            </select>
          </div>

          <div className="filter-group">
            <label>Tuyến bay:</label>
            <select 
              value={selectedRoute} 
              onChange={(e) => setSelectedRoute(e.target.value)}
            >
              {routes.map(route => (
                <option key={route} value={route}>
                  {route === 'ALL' ? 'Tất cả' : route}
                </option>
              ))}
            </select>
          </div>

          <button className="refresh-btn" onClick={loadFlights}>
            🔄 Làm mới
          </button>
        </div>

        {error && (
          <div className="error-message">
            <span className="error-icon">⚠️</span>
            <span>{error}</span>
          </div>
        )}

        {/* Flights Grid */}
        <div className="flights-grid">
          {filteredFlights.length > 0 ? (
            filteredFlights.map((flight) => (
              <div key={flight.id} className="flight-card">
                <div className="flight-header">
                  <div className="flight-number-section">
                    <span className="flight-number">{flight.flightNumber}</span>
                    <span className="airline-name">{flight.airline}</span>
                  </div>
                  {getStatusBadge(flight.status)}
                </div>

                <div className="flight-route">
                  <div className="route-point">
                    <span className="airport-code">{flight.origin}</span>
                    <span className="time">{formatDateTime(flight.departTime)}</span>
                  </div>
                  
                  <div className="route-line">
                    <div className="plane-icon">✈️</div>
                    <div className="duration">
                      {flight.durationMinutes ? `${Math.floor(flight.durationMinutes / 60)}h ${flight.durationMinutes % 60}m` : 'N/A'}
                    </div>
                  </div>
                  
                  <div className="route-point">
                    <span className="airport-code">{flight.destination}</span>
                    <span className="time">{formatDateTime(flight.arriveTime)}</span>
                  </div>
                </div>

                <div className="flight-details">
                  <div className="detail-item">
                    <span className="detail-icon">🛫</span>
                    <div>
                      <p className="detail-label">Loại máy bay</p>
                      <p className="detail-value">{flight.aircraftType || 'N/A'}</p>
                    </div>
                  </div>
                  
                  <div className="detail-item">
                    <span className="detail-icon">💺</span>
                    <div>
                      <p className="detail-label">Hạng ghế</p>
                      <p className="detail-value">{flight.cabinClass}</p>
                    </div>
                  </div>
                  
                  <div className="detail-item">
                    <span className="detail-icon">🪑</span>
                    <div>
                      <p className="detail-label">Ghế trống</p>
                      <p className="detail-value">
                        {flight.availableSeats}/{flight.totalSeats}
                      </p>
                    </div>
                  </div>
                </div>

                <div className="flight-footer">
                  <div className="price-section">
                    <span className="price-label">Giá vé từ</span>
                    <span className="price-value">
                      {formatCurrency((flight.totalPrice || flight.baseFare || 0))}
                    </span>
                  </div>
                  
                  {flight.status === 'SCHEDULED' && flight.availableSeats > 0 && (
                    <button 
                      className="book-btn"
                      onClick={() => handleBookFlight(flight)}
                    >
                      Đặt vé ngay
                    </button>
                  )}
                </div>
              </div>
            ))
          ) : (
            <div className="empty-state">
              <span className="empty-icon">✈️</span>
              <p>Không tìm thấy chuyến bay nào</p>
              {(selectedAirline !== 'ALL' || selectedRoute !== 'ALL' || searchTerm) && (
                <button 
                  className="clear-filters-btn"
                  onClick={() => {
                    setSelectedAirline('ALL');
                    setSelectedRoute('ALL');
                    setSearchTerm('');
                  }}
                >
                  Xóa bộ lọc
                </button>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default AirlineInformation;
