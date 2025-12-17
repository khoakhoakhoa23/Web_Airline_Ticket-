import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { bookingService } from '../services/api';
import '../styles/pages/Confirmation.css';

const Confirmation = () => {
  const { bookingId } = useParams();
  const [booking, setBooking] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchBooking = async () => {
      try {
        const response = await bookingService.getBookingById(bookingId);
        setBooking(response.data);
      } catch (error) {
        console.error('Error fetching booking:', error);
        // Error will be handled by showing "Booking not found" message
      } finally {
        setLoading(false);
      }
    };

    if (bookingId) {
      fetchBooking();
    }
  }, [bookingId]);

  if (loading) {
    return <div className="confirmation-page">Loading...</div>;
  }

  if (!booking) {
    return (
      <div className="confirmation-page">
        <div className="confirmation-container">
          <h2>Booking not found</h2>
          <Link to="/" className="home-link">Back to Home</Link>
        </div>
      </div>
    );
  }

  return (
    <div className="confirmation-page">
      <div className="confirmation-container">
        <div className="success-icon">✓</div>
        <h1>Booking Confirmed!</h1>
        <p className="booking-code">Booking Code: <strong>{booking.bookingCode}</strong></p>
        
        <div className="booking-details">
          <h3>Booking Details</h3>
          <div className="detail-row">
            <span>Status:</span>
            <span className="status">{booking.status}</span>
          </div>
          <div className="detail-row">
            <span>Total Amount:</span>
            <span className="amount">AU$ {booking.totalAmount?.toLocaleString() || '0'}</span>
          </div>
          <div className="detail-row">
            <span>Currency:</span>
            <span>{booking.currency}</span>
          </div>
        </div>

        <div className="actions">
          <Link to="/my-bookings" className="button primary">View My Bookings</Link>
          <Link to="/" className="button secondary">Back to Home</Link>
        </div>
      </div>
    </div>
  );
};

export default Confirmation;

