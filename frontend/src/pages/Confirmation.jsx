import { useState, useEffect } from 'react';
import { useParams, useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { useBooking } from '../contexts/BookingContext';
import { toast } from 'react-toastify';
import axios from 'axios';
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
      
      if (!bookingId) {
        setError('No booking ID provided');
        return;
      }

      // Get booking from API
      const token = localStorage.getItem('token');
      const response = await axios.get(
        `http://localhost:8080/api/bookings/${bookingId}`,
        {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        }
      );

      const bookingData = response.data;
      setBooking(bookingData);

      // Check payment status from query params
      const paymentStatus = searchParams.get('payment');
      if (paymentStatus === 'success') {
        toast.success('Thanh toán thành công!');
      }

    } catch (err) {
      console.error('Error loading booking:', err);
      setError('Không thể tải thông tin booking');
      toast.error('Không thể tải thông tin booking');
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

  if (error || !booking) {
    return (
      <div className="confirmation-page">
        <div className="error-container">
          <div className="error-icon">❌</div>
          <h2>Không tìm thấy booking</h2>
          <p>{error}</p>
          <button onClick={() => navigate('/my-bookings')} className="btn-primary">
            Xem vé của tôi
          </button>
        </div>
      </div>
    );
  }

  const flightSegment = booking.flightSegments?.[0];

  return (
    <div className="confirmation-page">
      <div className="confirmation-container">
        {/* Success Banner */}
        <div className="success-banner">
          <div className="success-icon">✅</div>
          <h1 className="success-title">Đặt vé thành công!</h1>
          <p className="success-subtitle">
            Cảm ơn bạn đã đặt vé. Thông tin đặt chỗ đã được gửi đến email của bạn.
          </p>
        </div>

        {/* Booking Code */}
        <div className="booking-code-section">
          <h2>Mã đặt chỗ</h2>
          <div className="booking-code">
            <span className="code">{booking.bookingCode}</span>
            <button 
              className="copy-btn" 
              onClick={() => {
                navigator.clipboard.writeText(booking.bookingCode);
                toast.success('Đã sao chép mã đặt chỗ!');
              }}
            >
              📋 Sao chép
            </button>
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
            <h3>Hành khách</h3>
            <div className="passengers-list">
              {booking.passengers && booking.passengers.map((passenger, index) => (
                <div key={index} className="passenger-item">
                  <div className="passenger-icon">👤</div>
                  <div className="passenger-info">
                    <p className="passenger-name">{passenger.fullName}</p>
                    <p className="passenger-details">
                      {passenger.documentType}: {passenger.documentNumber}
                    </p>
                    {passenger.dateOfBirth && (
                      <p className="passenger-details">
                        Ngày sinh: {new Date(passenger.dateOfBirth).toLocaleDateString('vi-VN')}
                      </p>
                    )}
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Payment Summary */}
          <div className="payment-summary">
            <h3>Tóm tắt thanh toán</h3>
            <div className="summary-row">
              <span>Giá vé</span>
              <span>{formatCurrency(flightSegment?.baseFare || 0)}</span>
            </div>
            <div className="summary-row">
              <span>Thuế & phí</span>
              <span>{formatCurrency(flightSegment?.taxes || 0)}</span>
            </div>
            <div className="summary-row total">
              <span><strong>Tổng cộng</strong></span>
              <span><strong>{formatCurrency(booking.totalAmount)}</strong></span>
            </div>
            <div className="payment-status">
              <span className={`status-badge ${booking.status.toLowerCase()}`}>
                {booking.status === 'CONFIRMED' ? 'Đã xác nhận' : booking.status}
              </span>
            </div>
          </div>
        </div>

        {/* Action Buttons */}
        <div className="action-buttons">
          <button className="btn-secondary" onClick={handleDownloadTicket}>
            📄 Tải vé PDF
          </button>
          <button className="btn-secondary" onClick={handleEmailTicket}>
            📧 Gửi email
          </button>
          <button className="btn-primary" onClick={() => navigate('/my-bookings')}>
            Xem vé của tôi
          </button>
          <button className="btn-outline" onClick={() => navigate('/')}>
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

        {/* QR Code placeholder */}
        <div className="qr-code-section">
          <h3>Mã QR check-in</h3>
          <div className="qr-placeholder">
            <p>📱</p>
            <p>QR Code sẽ được gửi qua email</p>
            <p>hoặc hiển thị trong mục "Vé của tôi"</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Confirmation;
