import { useState, useEffect } from 'react';
import { useParams, useNavigate, useSearchParams, Link } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { useBooking } from '../contexts/BookingContext';
import { toast } from 'react-toastify';
import { bookingService, ticketService, seatSelectionService } from '../services/api';
import Barcode from '../components/Barcode';
import '../styles/pages/Confirmation.css';

/**
 * Confirmation Page
 * Displays booking confirmation after successful payment
 */
const Confirmation = () => {
  const { bookingId } = useParams();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { user } = useAuth();
  const { getBookingById, resetBooking } = useBooking();
  
  const [booking, setBooking] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [ticket, setTicket] = useState(null);
  const [seatSelections, setSeatSelections] = useState({});

  useEffect(() => {
    loadBooking();
    
    // Clear booking context after successful booking
    return () => {
      // resetBooking(); // Uncomment if you want to clear on unmount
    };
  }, [bookingId]);

  const loadBooking = async () => {
    try {
      setLoading(true);
      setError(null);
      
      if (!bookingId) {
        setError('No booking ID provided');
        return;
      }

      // Get booking from API using bookingService
      const response = await bookingService.getBookingById(bookingId);
      
      // Check if response and data exist
      if (!response || !response.data) {
        setError('Không tìm thấy thông tin booking');
        toast.error('Không tìm thấy thông tin booking');
        return;
      }
      
      const bookingData = response.data;
      setBooking(bookingData);

      // Check payment status from query params
      const paymentStatus = searchParams.get('payment');
      if (paymentStatus === 'success') {
        toast.success('Thanh toán thành công!');
      } else if (paymentStatus === 'pending') {
        toast.info('Yêu cầu thanh toán đã được gửi. Đang chờ admin duyệt...');
      }

      // Fetch ticket if booking is confirmed
      if (bookingData.status === 'CONFIRMED') {
        try {
          const ticketResponse = await ticketService.getTicketsByBookingId(bookingId);
          if (ticketResponse && ticketResponse.data) {
            setTicket(ticketResponse.data);
          }
        } catch (err) {
          console.log('No ticket found yet');
        }
      }

      // Seat selections are now included in passenger.seatNumber from API
      // But keep fallback for backward compatibility
      if (bookingData.passengers && bookingData.passengers.length > 0) {
        const seatMap = {};
        for (const passenger of bookingData.passengers) {
          // Use seatNumber from passenger DTO (populated by backend)
          if (passenger.seatNumber) {
            seatMap[passenger.id] = passenger.seatNumber;
          } else if (passenger.id) {
            // Fallback: fetch from API if not in DTO
            try {
              const seatResponse = await seatSelectionService.getSeatSelectionsByPassengerId(passenger.id);
              if (seatResponse && seatResponse.data) {
                const seats = seatResponse.data || [];
                if (seats.length > 0) {
                  seatMap[passenger.id] = seats[0].seatNumber;
                }
              }
            } catch (err) {
              console.error(`Failed to load seat for passenger ${passenger.id}:`, err);
            }
          }
        }
        setSeatSelections(seatMap);
      }

    } catch (err) {
      console.error('Error loading booking:', err);
      
      // Handle different error types
      const status = err.response?.status;
      const errorMessage = err.response?.data?.message || err.message;
      
      if (status === 403) {
        // Forbidden - User doesn't have permission
        setError('Bạn không có quyền xem booking này. Vui lòng đăng nhập lại hoặc kiểm tra phiên đăng nhập của bạn.');
        toast.error('Không có quyền truy cập. Vui lòng đăng nhập lại.');
      } else if (status === 401) {
        // Unauthorized - Not authenticated
        setError('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.');
        toast.error('Phiên đăng nhập đã hết hạn');
        // Redirect to login after a delay
        setTimeout(() => {
          navigate('/login');
        }, 2000);
      } else if (status === 404) {
        // Not found
        setError('Không tìm thấy booking với ID này.');
        toast.error('Không tìm thấy booking');
      } else {
        // Other errors
        setError(errorMessage || 'Không thể tải thông tin booking. Vui lòng thử lại sau.');
        toast.error(errorMessage || 'Không thể tải thông tin booking');
      }
    } finally {
      setLoading(false);
    }
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(amount);
  };

  const formatDateTime = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleString('vi-VN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const handleDownloadTicket = () => {
    toast.info('Tính năng tải vé PDF đang được phát triển...');
    // TODO: Implement PDF download
  };

  const handleEmailTicket = () => {
    toast.info('Vé điện tử đã được gửi đến email của bạn!');
    // TODO: Implement email sending
  };

  if (loading) {
    return (
      <div className="confirmation-page">
        <div className="loading-container">
          <div className="spinner"></div>
          <p>Đang tải thông tin...</p>
        </div>
      </div>
    );
  }

  if (error || (!booking && !loading)) {
    const isForbidden = error && error.includes('quyền');
    const isUnauthorized = error && error.includes('hết hạn');
    
    return (
      <div className="confirmation-page">
        <div className="error-container">
          <div className={`error-icon ${isForbidden ? 'forbidden' : isUnauthorized ? 'unauthorized' : ''}`}>
            {isForbidden ? '🚫' : isUnauthorized ? '🔒' : '❌'}
          </div>
          <h2>
            {isForbidden 
              ? 'Không có quyền truy cập' 
              : isUnauthorized 
              ? 'Phiên đăng nhập đã hết hạn'
              : 'Không tìm thấy booking'}
          </h2>
          <p className="error-message">{error || 'Không tìm thấy booking'}</p>
          <div className="error-actions">
            {isUnauthorized ? (
              <button onClick={() => navigate('/login')} className="btn-primary">
                <span className="icon">🔑</span>
                Đăng nhập lại
              </button>
            ) : (
              <button onClick={() => navigate('/my-bookings')} className="btn-primary">
                <span className="icon">📋</span>
                Xem vé của tôi
              </button>
            )}
            <button onClick={() => navigate('/')} className="btn-outline">
              <span className="icon">🏠</span>
              Về trang chủ
            </button>
          </div>
        </div>
      </div>
    );
  }

  const flightSegment = booking.flightSegments?.[0];
  const isConfirmed = booking.status === 'CONFIRMED';
  const isPending = booking.status === 'PENDING' || booking.status === 'PENDING_PAYMENT';
  const isCancelled = booking.status === 'CANCELLED' || booking.status === 'EXPIRED';

  return (
    <div className="confirmation-page">
      <div className="confirmation-container">
        {/* Success Banner - Only show for confirmed bookings */}
        {isConfirmed && (
          <div className="success-banner">
            <div className="success-icon">✅</div>
            <h1 className="success-title">Đặt vé thành công!</h1>
            <p className="success-subtitle">
              Cảm ơn bạn đã đặt vé. Thông tin đặt chỗ đã được gửi đến email của bạn.
            </p>
          </div>
        )}

        {/* Pending Banner */}
        {isPending && (
          <div className="pending-banner">
            <div className="pending-icon">⏳</div>
            <h1 className="pending-title">Đang chờ duyệt thanh toán</h1>
            <p className="pending-subtitle">
              Yêu cầu thanh toán của bạn đã được gửi và đang chờ admin duyệt. 
              Bạn sẽ nhận được thông báo khi đơn hàng được duyệt và vé được xuất.
            </p>
          </div>
        )}

        {/* Cancelled Banner */}
        {isCancelled && (
          <div className="cancelled-banner">
            <div className="cancelled-icon">❌</div>
            <h1 className="cancelled-title">Đặt chỗ đã bị hủy</h1>
            <p className="cancelled-subtitle">
              Đặt chỗ này đã bị hủy hoặc hết hạn.
            </p>
          </div>
        )}

        {/* Booking Code */}
        <div className="booking-code-section">
          <h2>
            <span className="icon">📋</span>
            Mã đặt chỗ
          </h2>
          <div className="booking-code">
            <span className="code">{booking.bookingCode || 'N/A'}</span>
            <button 
              className="copy-btn" 
              onClick={() => {
                if (booking.bookingCode) {
                  navigator.clipboard.writeText(booking.bookingCode);
                  toast.success('Đã sao chép mã đặt chỗ!');
                }
              }}
            >
              <span className="icon">📋</span>
              Sao chép
            </button>
          </div>
          <div className="status-badge-container">
            <span className={`status-badge-large ${isConfirmed ? 'confirmed' : isPending ? 'pending' : 'cancelled'}`}>
              {isConfirmed && <span className="icon">✅</span>}
              {isPending && <span className="icon">⏳</span>}
              {isCancelled && <span className="icon">❌</span>}
              <span>{booking.status}</span>
            </span>
          </div>
          <p className="code-note">
            Vui lòng lưu lại mã này để check-in và tra cứu vé
          </p>
        </div>

        {/* Booking Details */}
        <div className="booking-details-section">
          <h2>Thông tin chuyến bay</h2>
          
          {flightSegment && (
            <div className="flight-card">
              <div className="flight-header">
                <div className="airline-info">
                  <h3>{flightSegment.airline}</h3>
                  <p className="flight-number">Chuyến bay: {flightSegment.flightNumber}</p>
                </div>
                <div className="cabin-class">
                  <span className="badge">{flightSegment.cabinClass}</span>
                </div>
              </div>

              <div className="flight-route">
                <div className="location">
                  <div className="location-code">{flightSegment.origin}</div>
                  <div className="location-time">
                    {formatDateTime(flightSegment.departTime)}
                  </div>
                </div>

                <div className="route-line">
                  <div className="line"></div>
                  <div className="plane-icon">✈️</div>
                  <div className="line"></div>
                </div>

                <div className="location">
                  <div className="location-code">{flightSegment.destination}</div>
                  <div className="location-time">
                    {formatDateTime(flightSegment.arriveTime)}
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* Passengers */}
          <div className="passengers-section">
            <h3>
              <span className="icon">👥</span>
              Hành khách
            </h3>
            <div className="passengers-list">
              {booking.passengers && booking.passengers.map((passenger, index) => {
                const seatNumber = passenger.seatNumber || seatSelections[passenger.id];
                return (
                  <div key={index} className="passenger-item">
                    <div className="passenger-icon">👤</div>
                    <div className="passenger-info">
                      <p className="passenger-name">{passenger.fullName || 'N/A'}</p>
                      <div className="passenger-details-grid">
                        {passenger.documentType && passenger.documentNumber && (
                          <div className="detail-item">
                            <span className="detail-label">
                              <span className="icon">🆔</span>
                              {passenger.documentType}:
                            </span>
                            <span className="detail-value">{passenger.documentNumber}</span>
                          </div>
                        )}
                        {passenger.dateOfBirth && (
                          <div className="detail-item">
                            <span className="detail-label">
                              <span className="icon">📅</span>
                              Ngày sinh:
                            </span>
                            <span className="detail-value">
                              {new Date(passenger.dateOfBirth).toLocaleDateString('vi-VN')}
                            </span>
                          </div>
                        )}
                        {passenger.gender && (
                          <div className="detail-item">
                            <span className="detail-label">
                              <span className="icon">⚧️</span>
                              Giới tính:
                            </span>
                            <span className="detail-value">{passenger.gender}</span>
                          </div>
                        )}
                        {seatNumber && (
                          <div className="detail-item highlight">
                            <span className="detail-label">
                              <span className="icon">💺</span>
                              Ghế ngồi:
                            </span>
                            <span className="detail-value seat-number">{seatNumber}</span>
                          </div>
                        )}
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          </div>

          {/* Ticket Information */}
          {ticket && isConfirmed && (
            <div className="ticket-section">
              <h3>
                <span className="icon">🎫</span>
                Thông tin vé điện tử
              </h3>
              <div className="ticket-info">
                <div className="ticket-row highlight">
                  <span className="ticket-label">
                    <span className="icon">🎫</span>
                    PNR:
                  </span>
                  <span className="ticket-value">{ticket.pnr || 'N/A'}</span>
                </div>
                <div className="ticket-row highlight">
                  <span className="ticket-label">
                    <span className="icon">🎫</span>
                    E-Ticket Number:
                  </span>
                  <span className="ticket-value">{ticket.eticketNumber || 'N/A'}</span>
                </div>
                {ticket.issuedAt && (
                  <div className="ticket-row">
                    <span className="ticket-label">
                      <span className="icon">📅</span>
                      Ngày phát hành:
                    </span>
                    <span className="ticket-value">{formatDateTime(ticket.issuedAt)}</span>
                  </div>
                )}
              </div>
            </div>
          )}

          {/* Payment Summary */}
          <div className="payment-summary">
            <h3>
              <span className="icon">💰</span>
              Tóm tắt thanh toán
            </h3>
            <div className="summary-content">
              <div className="summary-row">
                <span className="summary-label">
                  <span className="icon">✈️</span>
                  Giá vé
                </span>
                <span className="summary-value">{formatCurrency(flightSegment?.baseFare || 0)}</span>
              </div>
              <div className="summary-row">
                <span className="summary-label">
                  <span className="icon">📋</span>
                  Thuế & phí
                </span>
                <span className="summary-value">{formatCurrency(flightSegment?.taxes || 0)}</span>
              </div>
              {booking.seatPrice && booking.seatPrice > 0 && (
                <div className="summary-row">
                  <span className="summary-label">
                    <span className="icon">💺</span>
                    Phí chọn ghế
                  </span>
                  <span className="summary-value">{formatCurrency(booking.seatPrice)}</span>
                </div>
              )}
              <div className="summary-row total">
                <span className="summary-label">
                  <strong>Tổng cộng</strong>
                </span>
                <span className="summary-value total-amount">
                  <strong>{formatCurrency(booking.totalAmount || 0)}</strong>
                </span>
              </div>
              {booking.holdExpiresAt && isPending && (
                <div className="expiry-warning">
                  <span className="icon">⏰</span>
                  <span>Hết hạn: {formatDateTime(booking.holdExpiresAt)}</span>
                </div>
              )}
            </div>
          </div>
        </div>

        {/* Action Buttons */}
        <div className="action-buttons">
          {isConfirmed && (
            <>
              <button className="btn-secondary" onClick={handleDownloadTicket}>
                <span className="icon">📄</span>
                Tải vé PDF
              </button>
              <button className="btn-secondary" onClick={handleEmailTicket}>
                <span className="icon">📧</span>
                Gửi email
              </button>
            </>
          )}
          {isPending && booking.status === 'PENDING_PAYMENT' && (
            <div className="pending-payment-info">
              <p style={{ color: '#f59e0b', marginBottom: '10px' }}>
                ⏳ Đang chờ admin duyệt thanh toán. Vui lòng đợi thông báo.
              </p>
            </div>
          )}
          <button className="btn-primary" onClick={() => navigate('/my-bookings')}>
            <span className="icon">📋</span>
            Xem vé của tôi
          </button>
          <button className="btn-outline" onClick={() => navigate('/')}>
            <span className="icon">🏠</span>
            Về trang chủ
          </button>
        </div>

        {/* Important Notes */}
        <div className="important-notes">
          <h3>⚠️ Lưu ý quan trọng</h3>
          <ul>
            <li>Vui lòng có mặt tại sân bay ít nhất 2 giờ trước giờ khởi hành</li>
            <li>Mang theo CMND/Passport hợp lệ để làm thủ tục check-in</li>
            <li>Kiểm tra kỹ thông tin hành khách và giờ bay trước khi đến sân bay</li>
            <li>Liên hệ hotline: 1900-xxxx nếu cần hỗ trợ</li>
          </ul>
        </div>

        {/* Barcode for confirmed bookings */}
        {booking.status === 'CONFIRMED' && ticket && (
          <div className="barcode-section">
            <h3>Mã vạch vé</h3>
            <Barcode value={ticket.eticketNumber || booking.bookingCode} format="detailed" />
            <p className="barcode-note">
              Mã vạch này sẽ được sử dụng để check-in tại sân bay
            </p>
          </div>
        )}

        {/* QR Code placeholder for pending bookings */}
        {booking.status !== 'CONFIRMED' && (
          <div className="qr-code-section">
            <h3>Mã QR check-in</h3>
            <div className="qr-placeholder">
              <p>📱</p>
              <p>QR Code sẽ được gửi qua email sau khi vé được xác nhận</p>
              <p>hoặc hiển thị trong mục "Vé của tôi"</p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default Confirmation;
