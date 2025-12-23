import { useState, useEffect } from 'react';
import { getAllFlights, deleteFlight, createFlight } from '../../services/adminService';
import { seatSelectionService } from '../../services/api';
import './AdminFlights.css';

const AdminFlights = () => {
  const [flights, setFlights] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [showModal, setShowModal] = useState(false);
  const [showSeatsModal, setShowSeatsModal] = useState(false);
  const [selectedFlight, setSelectedFlight] = useState(null);
  const [bookedSeats, setBookedSeats] = useState([]);
  const [loadingSeats, setLoadingSeats] = useState(false);
  const [formData, setFormData] = useState({
    flightNumber: '',
    airline: '',
    origin: '',
    destination: '',
    departTime: '',
    arriveTime: '',
    cabinClass: 'ECONOMY',
    baseFare: '',
    taxes: '',
    availableSeats: '',
    totalSeats: '',
    status: 'SCHEDULED',
    aircraftType: '',
    durationMinutes: ''
  });

  useEffect(() => {
    loadFlights();
  }, [page]);

  const loadFlights = async () => {
    try {
      setLoading(true);
      const data = await getAllFlights(page, 20);
      setFlights(data.content || []);
      setTotalPages(data.totalPages || 0);
      setError(null);
    } catch (err) {
      console.error('Failed to load flights:', err);
      setError('Không thể tải danh sách chuyến bay');
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id, flightNumber, flight) => {
    // Chỉ cho phép xóa chuyến bay đã hết thời gian (arriveTime < now)
    if (flight && flight.arriveTime) {
      const arriveTime = new Date(flight.arriveTime);
      const now = new Date();
      
      if (arriveTime >= now) {
        alert('Chỉ có thể xóa chuyến bay đã hết thời gian bay. Chuyến bay này chưa đến thời gian đến.');
        return;
      }
    }
    
    if (!confirm(`Xóa chuyến bay ${flightNumber}?\n\nLưu ý: Lịch sử đặt vé của khách hàng sẽ được giữ lại.`)) return;

    try {
      await deleteFlight(id);
      alert('Đã xóa chuyến bay thành công! Lịch sử đặt vé đã được giữ lại.');
      loadFlights();
    } catch (err) {
      console.error('Failed to delete flight:', err);
      alert('Lỗi khi xóa chuyến bay: ' + (err.response?.data?.message || err.message));
    }
  };

  const handleViewBookedSeats = async (flight) => {
    try {
      setSelectedFlight(flight);
      setShowSeatsModal(true);
      setLoadingSeats(true);
      
      const response = await seatSelectionService.getBookedSeatsByFlightNumber(flight.flightNumber);
      setBookedSeats(response.data || []);
    } catch (err) {
      console.error('Failed to load booked seats:', err);
      alert('Không thể tải danh sách ghế đã đặt: ' + (err.response?.data?.message || err.message));
      setBookedSeats([]);
    } finally {
      setLoadingSeats(false);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    try {
      setLoading(true);
      
      // Convert string values to appropriate types
      const flightData = {
        ...formData,
        baseFare: parseFloat(formData.baseFare),
        taxes: parseFloat(formData.taxes),
        availableSeats: parseInt(formData.availableSeats),
        totalSeats: parseInt(formData.totalSeats),
        durationMinutes: parseInt(formData.durationMinutes)
      };
      
      await createFlight(flightData);
      alert('Đã tạo chuyến bay mới thành công!');
      setShowModal(false);
      resetForm();
      loadFlights();
    } catch (err) {
      console.error('Failed to create flight:', err);
      alert('Lỗi khi tạo chuyến bay: ' + (err.response?.data?.message || err.message));
    } finally {
      setLoading(false);
    }
  };

  const resetForm = () => {
    setFormData({
      flightNumber: '',
      airline: '',
      origin: '',
      destination: '',
      departTime: '',
      arriveTime: '',
      cabinClass: 'ECONOMY',
      baseFare: '',
      taxes: '',
      availableSeats: '',
      totalSeats: '',
      status: 'SCHEDULED',
      aircraftType: '',
      durationMinutes: ''
    });
  };

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

  if (loading && flights.length === 0) {
    return (
      <div className="admin-flights">
        <div className="loading-state">
          <div className="spinner"></div>
          <p>Đang tải danh sách chuyến bay...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="admin-flights">
      {/* Header */}
      <div className="page-header">
        <div>
          <h2 className="page-title">Quản Lý Chuyến Bay</h2>
          <p className="page-subtitle">Quản lý lịch bay và thông tin chuyến bay</p>
        </div>
        <div className="header-actions">
          <button onClick={() => setShowModal(true)} className="create-btn">
            ➕ Thêm chuyến bay
          </button>
          <button onClick={loadFlights} className="refresh-btn">
            🔄 Làm mới
          </button>
        </div>
      </div>

      {/* Stats */}
      <div className="flights-stats">
        <div className="stat-card">
          <span className="stat-icon">✈️</span>
          <div>
            <p className="stat-label">Tổng chuyến bay</p>
            <p className="stat-value">{flights.length}</p>
          </div>
        </div>
        <div className="stat-card">
          <span className="stat-icon">📅</span>
          <div>
            <p className="stat-label">Đã lên lịch</p>
            <p className="stat-value">{flights.filter(f => f.status === 'SCHEDULED').length}</p>
          </div>
        </div>
        <div className="stat-card">
          <span className="stat-icon">🛫</span>
          <div>
            <p className="stat-label">Đã bay</p>
            <p className="stat-value">
              {flights.filter(f => {
                if (!f.arriveTime) return false;
                const arriveTime = new Date(f.arriveTime);
                const now = new Date();
                return arriveTime < now; // Chuyến bay đã hết thời gian
              }).length}
            </p>
          </div>
        </div>
      </div>

      {error && (
        <div className="error-message">
          <span className="error-icon">⚠️</span>
          <span>{error}</span>
        </div>
      )}

      {/* Flights Table */}
      <div className="table-container">
        <table className="flights-table">
          <thead>
            <tr>
              <th>Số hiệu</th>
              <th>Hãng bay</th>
              <th>Tuyến bay</th>
              <th>Khởi hành</th>
              <th>Đến</th>
              <th>Giá vé</th>
              <th>Ghế trống</th>
              <th>Trạng thái</th>
              <th>Thao tác</th>
            </tr>
          </thead>
          <tbody>
            {flights.map((flight) => (
              <tr key={flight.id}>
                <td>
                  <span className="flight-number">{flight.flightNumber}</span>
                </td>
                <td>
                  <span className="airline">{flight.airline}</span>
                </td>
                <td>
                  <span className="route">
                    {flight.origin} → {flight.destination}
                  </span>
                </td>
                <td>
                  <span className="datetime">{formatDateTime(flight.departTime)}</span>
                </td>
                <td>
                  <span className="datetime">{formatDateTime(flight.arriveTime)}</span>
                </td>
                <td>
                  <span className="price">{formatCurrency(flight.totalPrice || flight.baseFare)}</span>
                </td>
                <td>
                  <span className="seats">
                    <span className="seats-available">{flight.availableSeats}</span>
                    <span className="seats-divider">/</span>
                    <span className="seats-total">{flight.totalSeats}</span>
                  </span>
                </td>
                <td>{getStatusBadge(flight.status)}</td>
                <td>
                  <div className="actions">
                    <button 
                      onClick={() => handleViewBookedSeats(flight)}
                      className="action-btn view"
                      title="Xem ghế đã đặt"
                    >
                      💺
                    </button>
                    <button 
                      onClick={() => {
                        const arriveTime = flight.arriveTime ? new Date(flight.arriveTime) : null;
                        const now = new Date();
                        const canDelete = arriveTime && arriveTime < now;
                        
                        if (canDelete) {
                          handleDelete(flight.id, flight.flightNumber, flight);
                        } else {
                          alert('Chỉ có thể xóa chuyến bay đã hết thời gian bay.');
                        }
                      }}
                      className="action-btn delete"
                      title={flight.arriveTime && new Date(flight.arriveTime) < new Date() 
                        ? "Xóa chuyến bay đã hết thời gian" 
                        : "Chuyến bay này chưa hết thời gian, không thể xóa"}
                      disabled={!flight.arriveTime || new Date(flight.arriveTime) >= new Date()}
                      style={{ 
                        opacity: (!flight.arriveTime || new Date(flight.arriveTime) >= new Date()) ? 0.5 : 1,
                        cursor: (!flight.arriveTime || new Date(flight.arriveTime) >= new Date()) ? 'not-allowed' : 'pointer'
                      }}
                    >
                      🗑️
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>

        {flights.length === 0 && !loading && (
          <div className="empty-state">
            <span className="empty-icon">✈️</span>
            <p>Không có chuyến bay nào</p>
          </div>
        )}
      </div>

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="pagination">
          <button 
            onClick={() => setPage(p => Math.max(0, p - 1))}
            disabled={page === 0}
            className="page-btn"
          >
            ← Trước
          </button>
          <span className="page-info">
            Trang {page + 1} / {totalPages}
          </span>
          <button 
            onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
            disabled={page >= totalPages - 1}
            className="page-btn"
          >
            Sau →
          </button>
        </div>
      )}

      {/* Create Flight Modal */}
      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Thêm Chuyến Bay Mới</h3>
              <button className="close-btn" onClick={() => setShowModal(false)}>×</button>
            </div>
            
            <form onSubmit={handleSubmit} className="flight-form">
              <div className="form-row">
                <div className="form-group">
                  <label>Số hiệu chuyến bay *</label>
                  <input
                    type="text"
                    name="flightNumber"
                    value={formData.flightNumber}
                    onChange={handleInputChange}
                    placeholder="VN123"
                    required
                  />
                </div>
                
                <div className="form-group">
                  <label>Hãng bay *</label>
                  <input
                    type="text"
                    name="airline"
                    value={formData.airline}
                    onChange={handleInputChange}
                    placeholder="Vietnam Airlines"
                    required
                  />
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>Điểm đi *</label>
                  <input
                    type="text"
                    name="origin"
                    value={formData.origin}
                    onChange={handleInputChange}
                    placeholder="HAN"
                    required
                  />
                </div>
                
                <div className="form-group">
                  <label>Điểm đến *</label>
                  <input
                    type="text"
                    name="destination"
                    value={formData.destination}
                    onChange={handleInputChange}
                    placeholder="SGN"
                    required
                  />
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>Thời gian khởi hành *</label>
                  <input
                    type="datetime-local"
                    name="departTime"
                    value={formData.departTime}
                    onChange={handleInputChange}
                    required
                  />
                </div>
                
                <div className="form-group">
                  <label>Thời gian đến *</label>
                  <input
                    type="datetime-local"
                    name="arriveTime"
                    value={formData.arriveTime}
                    onChange={handleInputChange}
                    required
                  />
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>Hạng ghế *</label>
                  <select
                    name="cabinClass"
                    value={formData.cabinClass}
                    onChange={handleInputChange}
                    required
                  >
                    <option value="ECONOMY">Economy</option>
                    <option value="BUSINESS">Business</option>
                    <option value="FIRST">First Class</option>
                  </select>
                </div>
                
                <div className="form-group">
                  <label>Loại máy bay *</label>
                  <input
                    type="text"
                    name="aircraftType"
                    value={formData.aircraftType}
                    onChange={handleInputChange}
                    placeholder="Boeing 787"
                    required
                  />
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>Giá vé cơ bản *</label>
                  <input
                    type="number"
                    name="baseFare"
                    value={formData.baseFare}
                    onChange={handleInputChange}
                    placeholder="1000000"
                    required
                    min="0"
                  />
                </div>
                
                <div className="form-group">
                  <label>Thuế *</label>
                  <input
                    type="number"
                    name="taxes"
                    value={formData.taxes}
                    onChange={handleInputChange}
                    placeholder="200000"
                    required
                    min="0"
                  />
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>Số ghế khả dụng *</label>
                  <input
                    type="number"
                    name="availableSeats"
                    value={formData.availableSeats}
                    onChange={handleInputChange}
                    placeholder="150"
                    required
                    min="0"
                  />
                </div>
                
                <div className="form-group">
                  <label>Tổng số ghế *</label>
                  <input
                    type="number"
                    name="totalSeats"
                    value={formData.totalSeats}
                    onChange={handleInputChange}
                    placeholder="180"
                    required
                    min="0"
                  />
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>Thời gian bay (phút) *</label>
                  <input
                    type="number"
                    name="durationMinutes"
                    value={formData.durationMinutes}
                    onChange={handleInputChange}
                    placeholder="120"
                    required
                    min="0"
                  />
                </div>
                
                <div className="form-group">
                  <label>Trạng thái *</label>
                  <select
                    name="status"
                    value={formData.status}
                    onChange={handleInputChange}
                    required
                  >
                    <option value="SCHEDULED">Đã lên lịch</option>
                    <option value="BOARDING">Đang lên máy bay</option>
                    <option value="DEPARTED">Đã cất cánh</option>
                    <option value="ARRIVED">Đã hạ cánh</option>
                    <option value="CANCELLED">Đã hủy</option>
                    <option value="DELAYED">Bị trễ</option>
                  </select>
                </div>
              </div>

              <div className="form-actions">
                <button type="button" className="cancel-btn" onClick={() => { setShowModal(false); resetForm(); }}>
                  Hủy
                </button>
                <button type="submit" className="submit-btn" disabled={loading}>
                  {loading ? 'Đang tạo...' : 'Tạo chuyến bay'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Booked Seats Modal */}
      {showSeatsModal && selectedFlight && (
        <div className="modal-overlay" onClick={() => { setShowSeatsModal(false); setSelectedFlight(null); setBookedSeats([]); }}>
          <div className="modal-content seats-modal" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Ghế Đã Đặt - {selectedFlight.flightNumber}</h3>
              <button className="close-btn" onClick={() => { setShowSeatsModal(false); setSelectedFlight(null); setBookedSeats([]); }}>×</button>
            </div>
            
            <div className="seats-modal-body">
              <div className="flight-info-summary">
                <p><strong>Hãng bay:</strong> {selectedFlight.airline}</p>
                <p><strong>Tuyến bay:</strong> {selectedFlight.origin} → {selectedFlight.destination}</p>
                <p><strong>Ngày khởi hành:</strong> {formatDateTime(selectedFlight.departTime)}</p>
              </div>

              {loadingSeats ? (
                <div className="loading-seats">
                  <div className="spinner"></div>
                  <p>Đang tải danh sách ghế...</p>
                </div>
              ) : bookedSeats.length === 0 ? (
                <div className="no-booked-seats">
                  <p>Chưa có ghế nào được đặt cho chuyến bay này.</p>
                </div>
              ) : (
                <div className="booked-seats-table-container">
                  <table className="booked-seats-table">
                    <thead>
                      <tr>
                        <th>Số ghế</th>
                        <th>Tên khách hàng</th>
                        <th>Mã đặt chỗ</th>
                        <th>Trạng thái</th>
                      </tr>
                    </thead>
                    <tbody>
                      {bookedSeats.map((seat, index) => (
                        <tr key={index}>
                          <td><strong>{seat.seatNumber}</strong></td>
                          <td>{seat.passengerName || 'N/A'}</td>
                          <td>{seat.bookingCode || 'N/A'}</td>
                          <td>
                            <span className={`status-badge ${seat.status === 'CONFIRMED' ? 'confirmed' : seat.status === 'PENDING' ? 'pending' : 'cancelled'}`}>
                              {seat.status || 'N/A'}
                            </span>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                  <div className="seats-summary">
                    <p><strong>Tổng số ghế đã đặt:</strong> {bookedSeats.length}</p>
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminFlights;

