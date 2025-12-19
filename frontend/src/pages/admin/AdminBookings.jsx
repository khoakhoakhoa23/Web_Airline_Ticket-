import { useState, useEffect } from 'react';
import { getAllBookings, approveBooking, cancelBooking, getPendingNotifications } from '../../services/adminService';
import { toast } from 'react-toastify';
import './AdminBookings.css';

const AdminBookings = () => {
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [statusFilter, setStatusFilter] = useState('');
  const [selectedBooking, setSelectedBooking] = useState(null);
  const [pendingNotifications, setPendingNotifications] = useState([]);

  useEffect(() => {
    loadBookings();
    loadPendingNotifications();
    
    // Refresh notifications every 10 seconds
    const interval = setInterval(() => {
      loadPendingNotifications();
    }, 10000);
    
    return () => clearInterval(interval);
  }, [page, statusFilter]);

  const loadBookings = async () => {
    try {
      setLoading(true);
      const data = await getAllBookings(page, 20, statusFilter || null);
      setBookings(data.content || []);
      setTotalPages(data.totalPages || 0);
      setError(null);
    } catch (err) {
      console.error('Failed to load bookings:', err);
      setError('Failed to load bookings');
    } finally {
      setLoading(false);
    }
  };

  const loadPendingNotifications = async () => {
    try {
      const notifications = await getPendingNotifications();
      setPendingNotifications(notifications || []);
    } catch (err) {
      console.error('Failed to load notifications:', err);
    }
  };

  const handleApproveBooking = async (id) => {
    if (!confirm('Are you sure you want to approve this booking?')) return;

    try {
      await approveBooking(id);
      toast.success('Booking approved successfully');
      loadBookings();
      loadPendingNotifications();
    } catch (err) {
      console.error('Failed to approve booking:', err);
      const errorMessage = err.response?.data?.message || err.message || 'Failed to approve booking';
      toast.error('Failed to approve booking: ' + errorMessage);
    }
  };

  const handleCancelBooking = async (id) => {
    if (!confirm('Are you sure you want to cancel this booking?')) return;

    try {
      await cancelBooking(id);
      alert('Booking cancelled successfully');
      loadBookings();
    } catch (err) {
      console.error('Failed to cancel booking:', err);
      alert('Failed to cancel booking: ' + (err.response?.data?.message || err.message));
    }
  };

  const getStatusBadge = (status) => {
    const statusConfig = {
      CONFIRMED: { color: '#10b981', bg: '#d1fae5', icon: '✅' },
      PENDING_PAYMENT: { color: '#f59e0b', bg: '#fef3c7', icon: '⏳' },
      CANCELLED: { color: '#ef4444', bg: '#fee2e2', icon: '❌' },
      PENDING: { color: '#6b7280', bg: '#f3f4f6', icon: '⏸️' }
    };

    const config = statusConfig[status] || statusConfig.PENDING;
    return (
      <span 
        className="status-badge" 
        style={{ color: config.color, background: config.bg }}
      >
        <span>{config.icon}</span>
        <span>{status}</span>
      </span>
    );
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(amount || 0);
  };

  const formatDate = (dateString) => {
    return new Date(dateString).toLocaleString('vi-VN');
  };

  if (loading && bookings.length === 0) {
    return (
      <div className="admin-bookings">
        <div className="loading-state">
          <div className="spinner"></div>
          <p>Loading bookings...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="admin-bookings">
      {/* Header */}
      <div className="page-header">
        <div>
          <h2 className="page-title">Booking Management</h2>
          <p className="page-subtitle">View and manage all flight bookings</p>
        </div>
        <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
          {pendingNotifications.length > 0 && (
            <div className="notification-badge" style={{
              background: '#f59e0b',
              color: 'white',
              padding: '8px 16px',
              borderRadius: '20px',
              fontWeight: '600',
              fontSize: '14px',
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              animation: 'pulse 2s infinite'
            }}>
              <span>🔔</span>
              <span>{pendingNotifications.length} booking{pendingNotifications.length > 1 ? 's' : ''} cần duyệt</span>
            </div>
          )}
          <button onClick={loadBookings} className="refresh-btn">
            🔄 Refresh
          </button>
        </div>
      </div>

      {/* Filters */}
      <div className="filters-bar">
        <div className="filter-group">
          <label>Filter by Status:</label>
          <select 
            value={statusFilter} 
            onChange={(e) => { setStatusFilter(e.target.value); setPage(0); }}
            className="filter-select"
          >
            <option value="">All Statuses</option>
            <option value="CONFIRMED">Confirmed</option>
            <option value="PENDING_PAYMENT">Pending Payment</option>
            <option value="CANCELLED">Cancelled</option>
            <option value="PENDING">Pending</option>
          </select>
        </div>
      </div>

      {error && (
        <div className="error-message">
          <span className="error-icon">⚠️</span>
          <span>{error}</span>
        </div>
      )}

      {/* Bookings Table */}
      <div className="table-container">
        <table className="bookings-table">
          <thead>
            <tr>
              <th>Booking Code</th>
              <th>User ID</th>
              <th>Status</th>
              <th>Amount</th>
              <th>Created At</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {bookings.map((booking) => (
              <tr key={booking.id}>
                <td>
                  <span className="booking-code">{booking.bookingCode}</span>
                </td>
                <td>
                  <span className="user-id">{booking.userId?.substring(0, 8)}...</span>
                </td>
                <td>{getStatusBadge(booking.status)}</td>
                <td>
                  <span className="amount">{formatCurrency(booking.totalAmount)}</span>
                </td>
                <td>{formatDate(booking.createdAt)}</td>
                <td>
                  <div className="actions">
                    <button 
                      onClick={() => setSelectedBooking(booking)}
                      className="action-btn view"
                      title="View details"
                    >
                      👁️
                    </button>
                    {(booking.status === 'PENDING' || booking.status === 'PENDING_PAYMENT') && (
                      <button 
                        onClick={() => handleApproveBooking(booking.id)}
                        className="action-btn approve"
                        title="Approve booking"
                      >
                        ✅
                      </button>
                    )}
                    {booking.status !== 'CANCELLED' && (
                      <button 
                        onClick={() => handleCancelBooking(booking.id)}
                        className="action-btn cancel"
                        title="Cancel booking"
                      >
                        ❌
                      </button>
                    )}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>

        {bookings.length === 0 && !loading && (
          <div className="empty-state">
            <span className="empty-icon">📭</span>
            <p>No bookings found</p>
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
            ← Previous
          </button>
          <span className="page-info">
            Page {page + 1} of {totalPages}
          </span>
          <button 
            onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
            disabled={page >= totalPages - 1}
            className="page-btn"
          >
            Next →
          </button>
        </div>
      )}

      {/* Booking Details Modal */}
      {selectedBooking && (
        <div className="modal-overlay" onClick={() => setSelectedBooking(null)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h3>Booking Details</h3>
              <button onClick={() => setSelectedBooking(null)} className="close-btn">×</button>
            </div>
            <div className="modal-body">
              <div className="detail-row">
                <span className="detail-label">Booking Code:</span>
                <span className="detail-value">{selectedBooking.bookingCode}</span>
              </div>
              <div className="detail-row">
                <span className="detail-label">Status:</span>
                {getStatusBadge(selectedBooking.status)}
              </div>
              <div className="detail-row">
                <span className="detail-label">Total Amount:</span>
                <span className="detail-value">{formatCurrency(selectedBooking.totalAmount)}</span>
              </div>
              <div className="detail-row">
                <span className="detail-label">Currency:</span>
                <span className="detail-value">{selectedBooking.currency}</span>
              </div>
              <div className="detail-row">
                <span className="detail-label">Created At:</span>
                <span className="detail-value">{formatDate(selectedBooking.createdAt)}</span>
              </div>
              {selectedBooking.flightSegments && selectedBooking.flightSegments.length > 0 && (
                <div className="flight-segments">
                  <h4>Flight Segments</h4>
                  {selectedBooking.flightSegments.map((segment, index) => (
                    <div key={index} className="segment-card">
                      <p><strong>{segment.airline} {segment.flightNumber}</strong></p>
                      <p>{segment.origin} → {segment.destination}</p>
                      <p>{formatDate(segment.departTime)}</p>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminBookings;

