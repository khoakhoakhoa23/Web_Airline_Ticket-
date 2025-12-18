import { useEffect, useState } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { bookingService, seatSelectionService } from '../services/api';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import '../styles/pages/MyBookings.css';

/**
 * My Bookings Page
 * 
 * Displays user's booking history
 * Allows viewing booking details
 */
const MyBookings = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [filter, setFilter] = useState('ALL'); // ALL, PENDING, CONFIRMED, CANCELLED
  const [seatSelections, setSeatSelections] = useState({}); // Map passengerId -> seatNumber

  useEffect(() => {
    const fetchBookings = async () => {
      // Check if user is logged in
      if (!user) {
        toast.error('Please login to view your bookings');
        navigate('/login');
        return;
      }

      if (!user.id) {
        setError('User ID not found');
        setLoading(false);
        return;
      }
      
      try {
        const response = await bookingService.getBookingsByUserId(user.id);
        const bookingsData = response.data || [];
        
        // Sort by creation date (newest first)
        bookingsData.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
        
        setBookings(bookingsData);
        
        // Fetch seat selections for all passengers
        const seatMap = {};
        for (const booking of bookingsData) {
          if (booking.passengers && booking.passengers.length > 0) {
            for (const passenger of booking.passengers) {
              if (passenger.id) {
                try {
                  const seatResponse = await seatSelectionService.getSeatSelectionsByPassengerId(passenger.id);
                  const seats = seatResponse.data || [];
                  if (seats.length > 0) {
                    seatMap[passenger.id] = seats[0].seatNumber; // Get first seat (usually one per passenger)
                  }
                } catch (err) {
                  console.error(`Failed to load seat for passenger ${passenger.id}:`, err);
                }
              }
            }
          }
        }
        setSeatSelections(seatMap);
      } catch (err) {
        console.error('Error fetching bookings:', err);
        setError(err.response?.data?.message || 'Failed to load bookings');
        toast.error('Failed to load bookings');
      } finally {
        setLoading(false);
      }
    };

    fetchBookings();
  }, [user, navigate]);

  /**
   * Format date for display
   */
  const formatDate = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { 
      year: 'numeric', 
      month: 'short', 
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  /**
   * Format price for display
   */
  const formatPrice = (price) => {
    if (!price) return '0';
    return new Intl.NumberFormat('vi-VN').format(price);
  };

  /**
   * Get status badge class
   */
  const getStatusClass = (status) => {
    switch (status) {
      case 'CONFIRMED':
        return 'status-confirmed';
      case 'PENDING':
      case 'PENDING_PAYMENT':
        return 'status-pending';
      case 'CANCELLED':
        return 'status-cancelled';
      default:
        return '';
    }
  };

  /**
   * Filter bookings by status
   */
  const filteredBookings = bookings.filter(booking => {
    if (filter === 'ALL') return true;
    return booking.status === filter;
  });

  // Loading state
  if (loading) {
    return (
      <div className="my-bookings-page">
        <div className="loading-container">
          <div className="loading-spinner"></div>
          <p>Loading your bookings...</p>
        </div>
      </div>
    );
  }

  // Error state
  if (error && bookings.length === 0) {
    return (
      <div className="my-bookings-page">
        <div className="error-container">
          <div className="error-icon">⚠️</div>
          <h2>Unable to Load Bookings</h2>
          <p className="error-message">{error}</p>
          <Link to="/" className="button primary">Back to Home</Link>
        </div>
      </div>
    );
  }

  return (
    <div className="my-bookings-page">
      <div className="bookings-container">
        <div className="page-header">
          <h1>My Bookings</h1>
          <p className="subtitle">View and manage your flight bookings</p>
        </div>

        {/* Filter Tabs */}
        {bookings.length > 0 && (
          <div className="filter-tabs">
            <button 
              className={`filter-tab ${filter === 'ALL' ? 'active' : ''}`}
              onClick={() => setFilter('ALL')}
            >
              All ({bookings.length})
            </button>
            <button 
              className={`filter-tab ${filter === 'CONFIRMED' ? 'active' : ''}`}
              onClick={() => setFilter('CONFIRMED')}
            >
              Confirmed ({bookings.filter(b => b.status === 'CONFIRMED').length})
            </button>
            <button 
              className={`filter-tab ${filter === 'PENDING' ? 'active' : ''}`}
              onClick={() => setFilter('PENDING')}
            >
              Pending ({bookings.filter(b => b.status === 'PENDING' || b.status === 'PENDING_PAYMENT').length})
            </button>
            <button 
              className={`filter-tab ${filter === 'CANCELLED' ? 'active' : ''}`}
              onClick={() => setFilter('CANCELLED')}
            >
              Cancelled ({bookings.filter(b => b.status === 'CANCELLED').length})
            </button>
          </div>
        )}

        {/* Bookings List */}
        {filteredBookings.length === 0 ? (
          <div className="no-bookings">
            <div className="no-bookings-icon">✈️</div>
            <h3>
              {filter === 'ALL' 
                ? "You don't have any bookings yet" 
                : `No ${filter.toLowerCase()} bookings`
              }
            </h3>
            <p>
              {filter === 'ALL'
                ? "Start your journey by booking your first flight!"
                : "Try selecting a different filter to see other bookings"
              }
            </p>
            {filter === 'ALL' && (
              <Link to="/" className="book-button">
                Search Flights
              </Link>
            )}
          </div>
        ) : (
          <div className="bookings-list">
            {filteredBookings.map((booking) => {
              const flightSegment = booking.flightSegments?.[0];
              
              return (
                <div key={booking.id} className="booking-card">
                  <div className="booking-header">
                    <div className="booking-code-section">
                      <span className="label">Booking Reference</span>
                      <span className="booking-code">{booking.bookingCode}</span>
                    </div>
                    <span className={`status-badge ${getStatusClass(booking.status)}`}>
                      {booking.status}
                    </span>
                  </div>

                  {/* Flight Info */}
                  {flightSegment && (
                    <div className="flight-info">
                      <div className="flight-route">
                        <div className="airport">
                          <span className="code">{flightSegment.origin}</span>
                        </div>
                        <div className="flight-arrow">✈️</div>
                        <div className="airport">
                          <span className="code">{flightSegment.destination}</span>
                        </div>
                      </div>
                      <div className="flight-meta">
                        <span>{flightSegment.airline} {flightSegment.flightNumber}</span>
                        <span>•</span>
                        <span>{flightSegment.cabinClass}</span>
                        <span>•</span>
                        <span>{formatDate(flightSegment.departTime)}</span>
                      </div>
                    </div>
                  )}

                  {/* Booking Details */}
                  <div className="booking-details">
                    <div className="detail-row">
                      <span className="label">Passengers:</span>
                      <span className="value">{booking.passengers?.length || 0}</span>
                    </div>
                    {booking.passengers && booking.passengers.length > 0 && (
                      <div className="detail-row">
                        <span className="label">Ghế đã đặt:</span>
                        <span className="value">
                          {(() => {
                            const seats = booking.passengers
                              .map(passenger => seatSelections[passenger.id])
                              .filter(Boolean);
                            return seats.length > 0 ? (
                              <span className="seats-list">
                                {seats.map((seat, idx) => (
                                  <span key={idx} className="seat-badge">
                                    {seat}
                                  </span>
                                ))}
                              </span>
                            ) : (
                              <span className="no-seat">Chưa chọn ghế</span>
                            );
                          })()}
                        </span>
                      </div>
                    )}
                    <div className="detail-row">
                      <span className="label">Total Amount:</span>
                      <span className="value amount">
                        {formatPrice(booking.totalAmount)} {booking.currency}
                      </span>
                    </div>
                    <div className="detail-row">
                      <span className="label">Created:</span>
                      <span className="value">{formatDate(booking.createdAt)}</span>
                    </div>
                    {booking.holdExpiresAt && booking.status === 'PENDING' && (
                      <div className="detail-row warning">
                        <span className="label">Expires:</span>
                        <span className="value">{formatDate(booking.holdExpiresAt)}</span>
                      </div>
                    )}
                  </div>

                  {/* Actions */}
                  <div className="booking-actions">
                    <Link to={`/booking/confirmation/${booking.id}`} className="button primary">
                      View Details
                    </Link>
                    {(booking.status === 'PENDING' || booking.status === 'PENDING_PAYMENT') && (
                      <Link to={`/booking/payment?booking_id=${booking.id}`} className="button secondary">
                        Complete Payment
                      </Link>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
};

export default MyBookings;

