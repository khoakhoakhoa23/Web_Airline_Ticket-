import { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { useBooking } from '../contexts/BookingContext';
import { bookingService, paymentService } from '../services/api';
import { toast } from 'react-toastify';
import '../styles/pages/Payment.css';

/**
 * Payment Page
 * 
 * Handles payment creation and redirect to payment provider
 * 
 * Flow:
 * 1. Get booking ID from URL params or localStorage
 * 2. Fetch booking details
 * 3. Display payment summary
 * 4. User accepts terms and clicks Pay
 * 5. Create payment via API
 * 6. Redirect to payment provider (Stripe Checkout)
 */
const Payment = () => {
  const { user } = useAuth();
  const { currentBooking, getBookingById } = useBooking();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  
  // State
  const [booking, setBooking] = useState(null);
  const [paymentMethod, setPaymentMethod] = useState('STRIPE');
  const [loading, setLoading] = useState(true);
  const [processing, setProcessing] = useState(false);
  const [error, setError] = useState('');
  const [acceptedTerms, setAcceptedTerms] = useState(false);

  /**
   * Fetch booking details on page load
   */
  useEffect(() => {
    const loadBooking = async () => {
      try {
        // Get booking ID from URL params or localStorage or context
        let bookingId = searchParams.get('booking_id');
        
        if (!bookingId) {
          // Try to get from localStorage
          const storedBookingId = localStorage.getItem('currentBookingId');
          if (storedBookingId) {
            bookingId = storedBookingId;
          } else if (currentBooking?.id) {
            // Try to get from context
            bookingId = currentBooking.id;
          } else {
            setError('No booking ID found. Please create a booking first.');
            setLoading(false);
            return;
          }
        }
        
        // Update URL if bookingId was from localStorage/context
        if (bookingId && !searchParams.get('booking_id')) {
          navigate(`/booking/payment?booking_id=${bookingId}`, { replace: true });
        }
        
        // Check if user is logged in
        if (!user) {
          toast.error('Please login to continue with payment');
          navigate('/login');
          return;
        }
        
        // Fetch booking details
        const response = await bookingService.getBookingById(bookingId);
        const bookingData = response.data;
        
        // Validate booking status
        if (bookingData.status === 'CONFIRMED') {
          setError('This booking has already been paid and confirmed.');
          setLoading(false);
          return;
        }
        
        if (bookingData.status === 'CANCELLED') {
          setError('This booking has been cancelled.');
          setLoading(false);
          return;
        }
        
        setBooking(bookingData);
      } catch (err) {
        console.error('Error fetching booking:', err);
        setError(err.response?.data?.message || 'Failed to load booking details.');
      } finally {
        setLoading(false);
      }
    };
    
    loadBooking();
  }, [searchParams, navigate, user, currentBooking]);

  /**
   * Handle payment submission
   * Creates PaymentIntent and returns clientSecret for frontend confirmation
   * 
   * Error Handling Fix:
   * - Problem: Frontend crashed with "Cannot read properties of null (reading 'data')"
   * - Cause: When API returns 402/error, err.response might be null or err.response.data might be null
   * - Solution: Use optional chaining and provide fallback error messages
   */
  const handlePayment = async () => {
    if (!acceptedTerms) {
      toast.error('Please accept the terms and conditions');
      return;
    }

    if (!user) {
      toast.error('Please login to continue');
      navigate('/login');
      return;
    }

    if (!booking) {
      toast.error('Booking not found');
      return;
    }

    setProcessing(true);
    setError('');

    try {
      // Create payment request
      const paymentData = {
        bookingId: booking.id,
        paymentMethod: paymentMethod,
        successUrl: `${window.location.origin}/booking/confirmation/${booking.id}?payment=success`,
        cancelUrl: `${window.location.origin}/booking/payment?booking_id=${booking.id}&payment=cancelled`,
      };

      // Call payment API
      // FIXED: Safe error handling - check if response exists before accessing data
      const response = await paymentService.createPayment(paymentData);
      
      // Validate response structure
      if (!response || !response.data) {
        throw new Error('Invalid response from payment server');
      }
      
      const payment = response.data;
      
      // Payment created successfully - waiting for admin approval
      if (payment.status === 'PENDING') {
        toast.success('Yêu cầu thanh toán đã được gửi! Đang chờ admin duyệt...');
        
        // Store payment ID for reference
        localStorage.setItem('pendingPaymentId', payment.paymentId);
        
        // Redirect to booking confirmation page with pending status
        navigate(`/booking/confirmation/${booking.id}?payment=pending`, { replace: true });
      } else if (payment.checkoutUrl) {
        // Fallback: If checkoutUrl is provided (for other payment methods)
        toast.success('Redirecting to payment gateway...');
        localStorage.setItem('pendingPaymentId', payment.paymentId);
        window.location.href = payment.checkoutUrl;
      } else {
        // Show success message
        toast.success(payment?.message || 'Payment request submitted successfully');
        navigate(`/booking/confirmation/${booking.id}?payment=pending`, { replace: true });
      }
    } catch (err) {
      console.error('Error creating payment:', err);
      
      // FIXED: Safe error message extraction with multiple fallbacks
      // Prevents crash when err.response or err.response.data is null
      let errorMessage = 'Payment failed. Please try again.';
      
      if (err.response) {
        // Response exists - try to extract message
        if (err.response.data) {
          // Data exists - use message from response
          errorMessage = err.response.data.message || 
                       err.response.data.error || 
                       `Payment failed: ${err.response.status} ${err.response.statusText}`;
        } else {
          // Response exists but no data - use status
          errorMessage = `Payment failed: ${err.response.status} ${err.response.statusText || 'Unknown error'}`;
        }
      } else if (err.message) {
        // No response but has message (network error, etc.)
        errorMessage = err.message;
      }
      
      setError(errorMessage);
      toast.error(errorMessage);
    } finally {
      setProcessing(false);
    }
  };

  /**
   * Calculate total amount from booking
   */
  const calculateTotal = () => {
    if (!booking) return 0;
    return booking.totalAmount || 0;
  };

  /**
   * Format price for display
   */
  const formatPrice = (price) => {
    if (!price) return '0';
    return new Intl.NumberFormat('vi-VN').format(price);
  };

  /**
   * Format date for display
   */
  const formatDate = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { 
      weekday: 'short', 
      year: 'numeric', 
      month: 'short', 
      day: 'numeric' 
    });
  };

  /**
   * Format time for display
   */
  const formatTime = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleTimeString('en-US', { 
      hour: '2-digit', 
      minute: '2-digit',
      hour12: false
    });
  };

  // Loading state
  if (loading) {
    return (
      <div className="payment-page">
        <div className="loading-container">
          <div className="loading-spinner"></div>
          <p>Loading booking details...</p>
        </div>
      </div>
    );
  }

  // Error state - no booking found
  if (error && !booking) {
    return (
      <div className="payment-page">
        <div className="error-container">
          <div className="error-icon">⚠️</div>
          <h2>Unable to Load Booking</h2>
          <p className="error-message">{error}</p>
          <button onClick={() => navigate('/my-bookings')} className="back-button">
            View My Bookings
          </button>
        </div>
      </div>
    );
  }

  const total = calculateTotal();
  const flightSegment = booking?.flightSegments?.[0] || null;

  return (
    <div className="payment-page">
      {/* Progress Bar */}
      <div className="progress-bar">
        <div className="progress-step completed">
          <div className="step-number">1</div>
          <span>Flight Selection</span>
        </div>
        <div className="progress-line"></div>
        <div className="progress-step completed">
          <div className="step-number">2</div>
          <span>Traveller Information</span>
        </div>
        <div className="progress-line"></div>
        <div className="progress-step active">
          <div className="step-number">3</div>
          <span>Payment</span>
        </div>
      </div>

      <div className="payment-container">
        <div className="main-content">
          <h2>Payment</h2>
          
          {/* Booking Summary */}
          <div className="booking-summary-section">
            <h3>Booking Summary</h3>
            <div className="summary-item">
              <span className="label">Booking Code:</span>
              <span className="value"><strong>{booking.bookingCode}</strong></span>
            </div>
            <div className="summary-item">
              <span className="label">Status:</span>
              <span className="value status-badge">{booking.status}</span>
            </div>
            {booking.holdExpiresAt && (
              <div className="summary-item">
                <span className="label">Hold Expires:</span>
                <span className="value">{formatDate(booking.holdExpiresAt)}</span>
              </div>
            )}
          </div>

          {/* Payment Methods */}
          <div className="payment-methods-section">
            <h3>Choose Payment Method</h3>
            <div className="payment-methods">
              <div className="payment-method">
                <label>
                  <input
                    type="radio"
                    name="paymentMethod"
                    value="STRIPE"
                    checked={paymentMethod === 'STRIPE'}
                    onChange={(e) => setPaymentMethod(e.target.value)}
                  />
                  <div className="payment-method-content">
                    <div>
                      <strong>Credit/Debit Card (Stripe)</strong>
                      <p>Secure payment via Stripe. Supports VISA, Mastercard, and more.</p>
                    </div>
                    <div className="payment-logos">
                      <span>💳 VISA</span>
                      <span>💳 Mastercard</span>
                    </div>
                  </div>
                </label>
              </div>

              <div className="payment-method disabled">
                <label>
                  <input
                    type="radio"
                    name="paymentMethod"
                    value="VNPAY"
                    disabled
                    checked={paymentMethod === 'VNPAY'}
                    onChange={(e) => setPaymentMethod(e.target.value)}
                  />
                  <div className="payment-method-content">
                    <div>
                      <strong>VNPay</strong>
                      <p className="coming-soon">Coming soon - Vietnamese payment gateway</p>
                    </div>
                    <div className="payment-logos">
                      <span>🏦 VNPay</span>
                    </div>
                  </div>
                </label>
              </div>

              <div className="payment-method disabled">
                <label>
                  <input
                    type="radio"
                    name="paymentMethod"
                    value="MOMO"
                    disabled
                    checked={paymentMethod === 'MOMO'}
                    onChange={(e) => setPaymentMethod(e.target.value)}
                  />
                  <div className="payment-method-content">
                    <div>
                      <strong>Momo E-Wallet</strong>
                      <p className="coming-soon">Coming soon - Vietnamese e-wallet</p>
                    </div>
                    <div className="payment-logos">
                      <span>📱 Momo</span>
                    </div>
                  </div>
                </label>
              </div>
            </div>
          </div>

          {/* Total Amount */}
          <div className="total-amount">
            <div className="amount-row">
              <span className="label">Currency:</span>
              <span className="value">{booking.currency}</span>
            </div>
            <div className="amount-row total">
              <span className="label">Total Amount:</span>
              <span className="value">{formatPrice(total)} {booking.currency}</span>
            </div>
          </div>

          {/* Error Message */}
          {error && (
            <div className="error-message">
              <span className="error-icon">⚠️</span>
              <span>{error}</span>
            </div>
          )}

          {/* Terms and Conditions */}
          <div className="terms-section">
            <label className="checkbox-label">
              <input
                type="checkbox"
                checked={acceptedTerms}
                onChange={(e) => setAcceptedTerms(e.target.checked)}
                required
              />
              <span>
                I have read and accept the travel conditions, fare rules, and airline's general terms and conditions. 
                I have verified that I have entered my booking information correctly.
              </span>
            </label>
            <p className="terms-info">
              By proceeding with payment, you agree to our <a href="/terms" target="_blank">Terms of Service</a> and <a href="/privacy" target="_blank">Privacy Policy</a>.
            </p>
          </div>

          {/* Payment Button */}
          <button
            onClick={handlePayment}
            className="pay-button"
            disabled={processing || !acceptedTerms}
          >
            {processing ? (
              <>
                <span className="spinner"></span>
                Processing...
              </>
            ) : (
              `Pay ${formatPrice(total)} ${booking.currency}`
            )}
          </button>

          {/* Payment Info */}
          <div className="payment-info">
            <p className="info-text">
              🔒 Your payment is secure and encrypted. You will be redirected to our payment partner to complete the transaction.
            </p>
          </div>
        </div>

        {/* Order Summary Sidebar */}
        <div className="order-summary">
          <h3>Order Summary</h3>
          
          {/* Flight Details */}
          {flightSegment && (
            <div className="order-section">
              <div className="order-item">
                <span className="order-icon">✈️</span>
                <div>
                  <p><strong>{flightSegment.airline} {flightSegment.flightNumber}</strong></p>
                  <p className="route">{flightSegment.origin} → {flightSegment.destination}</p>
                  <p className="date">{formatDate(flightSegment.departTime)}</p>
                  <p className="time">
                    {formatTime(flightSegment.departTime)} - {formatTime(flightSegment.arriveTime)}
                  </p>
                  <p className="cabin-class">Class: {flightSegment.cabinClass}</p>
                </div>
              </div>
            </div>
          )}
          
          {/* Passengers */}
          {booking.passengers && booking.passengers.length > 0 && (
            <div className="order-section">
              <h4>Passengers ({booking.passengers.length})</h4>
              {booking.passengers.map((passenger, index) => (
                <p key={index} className="passenger-name">
                  {index + 1}. {passenger.fullName}
                </p>
              ))}
            </div>
          )}
          
          {/* Price Breakdown */}
          <div className="order-section">
            <h4>Price Breakdown</h4>
            {flightSegment && (
              <>
                <div className="price-row">
                  <span>Base Fare:</span>
                  <span>{formatPrice(flightSegment.baseFare)} {booking.currency}</span>
                </div>
                <div className="price-row">
                  <span>Taxes & Fees:</span>
                  <span>{formatPrice(flightSegment.taxes)} {booking.currency}</span>
                </div>
              </>
            )}
            <div className="price-row total">
              <span><strong>Total:</strong></span>
              <span><strong>{formatPrice(total)} {booking.currency}</strong></span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Payment;

