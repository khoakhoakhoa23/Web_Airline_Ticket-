import { useEffect, useState } from 'react';
import { useAuth } from '../contexts/AuthContext';
import { bookingService } from '../services/api';
import { Link } from 'react-router-dom';
import '../styles/pages/MyBookings.css';

const MyBookings = () => {
  const { user } = useAuth();
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchBookings = async () => {
      if (!user?.id) return;
      
      try {
        const response = await bookingService.getBookingsByUserId(user.id);
        setBookings(response.data || []);
      } catch (error) {
        console.error('Error fetching bookings:', error);
        // Show error message to user if needed
      } finally {
        setLoading(false);
      }
    };

    fetchBookings();
  }, [user]);

  if (loading) {
    return <div className="my-bookings-page">Loading...</div>;
  }

  return (
    <div className="my-bookings-page">
      <div className="bookings-container">
        <h1>My Bookings</h1>
        {bookings.length === 0 ? (
          <div className="no-bookings">
            <p>You don't have any bookings yet.</p>
            <Link to="/" className="book-button">Book a Flight</Link>
          </div>
        ) : (
          <div className="bookings-list">
            {bookings.map((booking) => (
              <div key={booking.id} className="booking-card">
                <div className="booking-header">
                  <h3>Booking Code: {booking.bookingCode}</h3>
                  <span className={`status ${booking.status?.toLowerCase()}`}>
                    {booking.status}
                  </span>
                </div>
                <div className="booking-details">
                  <div className="detail-item">
                    <span>Total Amount:</span>
                    <strong>AU$ {booking.totalAmount?.toLocaleString() || '0'}</strong>
                  </div>
                  <div className="detail-item">
                    <span>Currency:</span>
                    <span>{booking.currency}</span>
                  </div>
                  <div className="detail-item">
                    <span>Created:</span>
                    <span>{new Date(booking.createdAt).toLocaleDateString()}</span>
                  </div>
                </div>
                <Link to={`/booking/confirmation/${booking.id}`} className="view-button">
                  View Details
                </Link>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default MyBookings;

