import { useEffect, useState } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { bookingService, seatSelectionService, ticketService } from '../services/api';
import { Link, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import Barcode from '../components/Barcode';
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
  const [tickets, setTickets] = useState({}); // Map bookingId -> ticket

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
        
        // Fetch seat selections for all bookings by booking ID (simpler and more reliable)
        const seatMap = {};
        for (const booking of bookingsData) {
          if (booking.id) {
            try {
              const seatsResponse = await seatSelectionService.getSeatSelectionsByBookingId(booking.id);
              const seats = seatsResponse.data || [];
              console.log(`[MyBookings] Booking ${booking.id} - Found ${seats.length} seat selections:`, seats);
              // Map seats to passengers
              for (const seat of seats) {
                if (seat.passengerId && seat.seatNumber) {
                  seatMap[seat.passengerId] = seat.seatNumber;
                  console.log(`[MyBookings] Mapped seat ${seat.seatNumber} to passenger ${seat.passengerId}`);
                }
              }
            } catch (err) {
              // Log error for debugging
              console.error(`[MyBookings] Error fetching seats for booking ${booking.id}:`, err);
              console.log(`No seats found for booking ${booking.id}`);
            }
          }
        }
        console.log(`[MyBookings] Final seatMap:`, seatMap);
        console.log(`[MyBookings] Passengers in bookings:`, bookingsData.map(b => b.passengers?.map(p => ({ id: p.id, name: p.fullName }))));
        setSeatSelections(seatMap);
        
        // Fetch tickets for confirmed bookings
        const ticketMap = {};
        for (const booking of bookingsData) {
          if (booking.status === 'CONFIRMED' && booking.id) {
            try {
              const ticketResponse = await ticketService.getTicketsByBookingId(booking.id);
              if (ticketResponse.data) {
                ticketMap[booking.id] = ticketResponse.data;
              }
            } catch (err) {
              // Ticket might not exist yet, ignore error
              console.log(`No ticket found for booking ${booking.id}`);
            }
          }
        }
        setTickets(ticketMap);
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
              const ticket = tickets[booking.id];
              const passengerNames = booking.passengers?.map(p => p.fullName).join(', ') || '';
              
              const isCancelled = booking.status === 'CANCELLED' || booking.status === 'EXPIRED';
              const isPending = booking.status === 'PENDING' || booking.status === 'PENDING_PAYMENT';
              const isConfirmed = booking.status === 'CONFIRMED';
              
              return (
                <div key={booking.id} className={`booking-card ${isCancelled ? 'booking-cancelled' : ''}`}>
                  <div className="booking-header">
                    <div className="booking-code-section">
                      <span className="label">
                        <span className="icon">📋</span>
                        Booking Reference
                      </span>
                      <span className="booking-code">{booking.bookingCode || 'N/A'}</span>
                    </div>
                    <span className={`status-badge ${getStatusClass(booking.status)}`}>
                      {isConfirmed && <span className="icon">✅</span>}
                      {isPending && <span className="icon">⏳</span>}
                      {isCancelled && <span className="icon">❌</span>}
                      <span>{booking.status}</span>
                    </span>
                  </div>

                  {/* Flight Info */}
                  {flightSegment && (
                    <div className="flight-info">
                      <div className="flight-route">
                        <div className="airport">
                          <span className="code">{flightSegment.origin || 'N/A'}</span>
                          <span className="airport-label">
                            <span className="icon">📍</span>
                            Điểm đi
                          </span>
                        </div>
                        <div className="flight-arrow">
                          <span className="icon">✈️</span>
                        </div>
                        <div className="airport">
                          <span className="code">{flightSegment.destination || 'N/A'}</span>
                          <span className="airport-label">
                            <span className="icon">📍</span>
                            Điểm đến
                          </span>
                        </div>
                      </div>
                      <div className="flight-meta">
                        <span className="meta-item">
                          <span className="icon">✈️</span>
                          <strong>{flightSegment.airline || 'N/A'} {flightSegment.flightNumber || 'N/A'}</strong>
                        </span>
                        <span className="meta-item">
                          <span className="icon">🪑</span>
                          {flightSegment.cabinClass || 'N/A'}
                        </span>
                        <span className="meta-item">
                          <span className="icon">🕐</span>
                          {formatDate(flightSegment.departTime)}
                        </span>
                      </div>
                    </div>
                  )}

                  {/* Passenger Names */}
                  {passengerNames && !isCancelled && (
                    <div className="passenger-names-section">
                      <span className="label">
                        <span className="icon">👥</span>
                        Hành khách:
                      </span>
                      <span className="value">{passengerNames}</span>
                    </div>
                  )}

                  {/* Seat Information - Only show for PENDING and CONFIRMED */}
                  {booking.passengers && booking.passengers.length > 0 && 
                   booking.status !== 'CANCELLED' && booking.status !== 'EXPIRED' && (
                    <div className="seats-section">
                      <h4 className="seats-section-title">
                        <span className="icon">💺</span>
                        Thông tin ghế ngồi
                      </h4>
                      <div className="seats-list-detailed">
                        {booking.passengers.map((passenger, idx) => {
                          // Use seatNumber from passenger DTO (populated by backend) or fallback to seatSelections map
                          const seatNumber = passenger.seatNumber || seatSelections[passenger.id];
                          const isPending = booking.status === 'PENDING' || booking.status === 'PENDING_PAYMENT';
                          const isConfirmed = booking.status === 'CONFIRMED';
                          
                          return (
                            <div key={idx} className="seat-passenger-item">
                              <div className="passenger-seat-info">
                                <div className="passenger-info-group">
                                  <span className="icon">👤</span>
                                  <span className="passenger-seat-name">{passenger.fullName}</span>
                                </div>
                                {seatNumber ? (
                                  <div className="seat-display-group">
                                    <span className="icon">🪑</span>
                                    <span className="seat-badge-large">{seatNumber}</span>
                                  </div>
                                ) : isPending ? (
                                  <div className="seat-pending-message">
                                    <span className="icon">⏳</span>
                                    <span className="pending-text">Ghế đã chọn – sẽ xác nhận sau thanh toán</span>
                                  </div>
                                ) : null}
                              </div>
                            </div>
                          );
                        })}
                      </div>
                    </div>
                  )}

                  {/* Booking Details */}
                  <div className="booking-details">
                    {ticket && isConfirmed && (
                      <>
                        <div className="detail-row highlight">
                          <span className="label">
                            <span className="icon">🎫</span>
                            PNR:
                          </span>
                          <span className="value ticket-code">{ticket.pnr || 'N/A'}</span>
                        </div>
                        <div className="detail-row highlight">
                          <span className="label">
                            <span className="icon">🎫</span>
                            E-Ticket Number:
                          </span>
                          <span className="value ticket-code">{ticket.eticketNumber || 'N/A'}</span>
                        </div>
                      </>
                    )}
                    <div className="detail-row highlight-amount">
                      <span className="label">
                        <span className="icon">💰</span>
                        Total Amount:
                      </span>
                      <span className="value amount">
                        {formatPrice(booking.totalAmount || 0)} {booking.currency || 'VND'}
                      </span>
                    </div>
                    <div className="detail-row">
                      <span className="label">
                        <span className="icon">📅</span>
                        Created:
                      </span>
                      <span className="value">{formatDate(booking.createdAt)}</span>
                    </div>
                    {booking.holdExpiresAt && isPending && (
                      <div className="detail-row warning">
                        <span className="label">
                          <span className="icon">⏰</span>
                          Expires:
                        </span>
                        <span className="value expires-warning">{formatDate(booking.holdExpiresAt)}</span>
                      </div>
                    )}
                  </div>

                  {/* Barcode for confirmed bookings */}
                  {booking.status === 'CONFIRMED' && ticket && (
                    <div className="barcode-section">
                      <Barcode value={ticket.eticketNumber || booking.bookingCode} format="simple" />
                    </div>
                  )}

                  {/* Actions - Hide for cancelled bookings */}
                  {!isCancelled && (
                    <div className="booking-actions">
                      <Link to={`/booking/confirmation/${booking.id}`} className="button primary">
                        <span className="icon">👁️</span>
                        View Details
                      </Link>
                      {isPending && (
                        <Link to={`/booking/payment?booking_id=${booking.id}`} className="button secondary payment-button">
                          <span className="icon">💳</span>
                          Complete Payment
                        </Link>
                      )}
                    </div>
                  )}
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

