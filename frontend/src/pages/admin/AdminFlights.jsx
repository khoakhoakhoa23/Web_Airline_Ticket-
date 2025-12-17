import { useState, useEffect } from 'react';
import { getAllFlights, deleteFlight } from '../../services/adminService';
import './AdminFlights.css';

const AdminFlights = () => {
  const [flights, setFlights] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

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

  const handleDelete = async (id, flightNumber) => {
    if (!confirm(`Xóa chuyến bay ${flightNumber}? Hành động này không thể hoàn tác.`)) return;

    try {
      await deleteFlight(id);
      alert('Đã xóa chuyến bay thành công!');
      loadFlights();
    } catch (err) {
      console.error('Failed to delete flight:', err);
      alert('Lỗi khi xóa chuyến bay: ' + (err.response?.data?.message || err.message));
    }
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
            <p className="stat-label">Đang bay</p>
            <p className="stat-value">{flights.filter(f => f.status === 'DEPARTED').length}</p>
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
                      onClick={() => handleDelete(flight.id, flight.flightNumber)}
                      className="action-btn delete"
                      title="Xóa chuyến bay"
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
    </div>
  );
};

export default AdminFlights;

