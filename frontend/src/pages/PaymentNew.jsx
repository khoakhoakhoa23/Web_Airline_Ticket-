import { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { bookingService, paymentService } from '../services/api';
import '../styles/pages/Payment.css';

/**
 * Payment Page
 * 
 * Handles payment creation and redirect to payment provider (Stripe)
 * 
 * Flow:
 * 1. Get booking ID from URL params
 * 2. Fetch booking details from API
 * 3. Display payment summary
 * 4. User clicks "Pay Now"
 * 5. Create payment via API
 * 6. Redirect to Stripe Checkout
 * 7. After payment, Stripe redirects to success/cancel page
 */
const Payment = () => {
  const { user, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  
  // State
  const [booking, setBooking] = useState(null);
  const [paymentMethod, setPaymentMethod] = useState('STRIPE');
  const [loading, setLoading] = useState(true);
  const [processing, setProcessing] = useState(false);
  const [error, setError] = useState('');
  const [acceptTerms, setAcceptTerms] = useState(false);

  /**
   * Fetch booking details on page load
   */
  useEffect(() => {
    // Check authentication
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    
    const fetchBooking = async () => {
      try {
        // Get booking ID from URL params
        let bookingId = searchParams.get('booking_id');
        
        if (!bookingId) {
          // Fallback: try localStorage
          bookingId = localStorage.getItem('currentBookingId');
        }
        
        if (!bookingId) {
          setError('No booking ID found. Please create a booking first.');
          setLoading(false);
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
    
    fetchBooking();
  }, [searchParams, navigate, isAuthenticated]);

  /**
   * Handle payment creation and redirect
   */
  const handlePayment = async () => {
    if (!acceptTerms) {
      setError('Please accept the terms and conditions to proceed.');
      return;
    }
    
    if (!booking) {
      setError('Booking data not available.');
      return;
    }
    
    setProcessing(true);
    setError('');
    
    try {
      // Create payment
      const response = await paymentService.createPayment({
        bookingId: booking.id,
        paymentMethod,
        successUrl: `${window.location.origin}/payment/success?booking_id=${booking.id}`,
        cancelUrl: `${window.location.origin}/payment/cancel?booking_id=${booking.id}`,
      });
      
      const paymentData = response.data;
      
      if (paymentData.checkoutUrl) {
        // Redirect to Stripe Checkout
        window.location.href = paymentData.checkoutUrl;
      } else {
        setError('Payment URL not received. Please try again.');
        setProcessing(false);
      }
    } catch (err) {
      console.error('Error creating payment:', err);
      setError(err.response?.data?.message || 'Failed to create payment. Please try again.');
      setProcessing(false);
    }
  };

  /**
   * Format price in VND
   */
  const formatPrice = (amount) => {
    if (!amount) return '0';
    return new Intl.NumberFormat('vi-VN').format(amount);
  };

  /**
   * Format date
   */
  const formatDate = (dateString) => {
    if (!dateString) return '';
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
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

  // Error state (no booking)
  if (error && !booking) {
    return (
      <div className="payment-page">
        <div className="error-container">
          <div className="error-icon">⚠️</div>
          <h2>Unable to Load Payment</h2>
          <p className="error-message">{error}</p>
          <button onClick={() => navigate('/')} className="back-button">
            Back to Home
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="payment-page">
      {/* Progress Bar */}
      <div className="progress-bar">
        <div className="progress-step completed">
          <div className="step-number">1</div>
          <span>Flight Selection</span>
        </div>
        <div className="progress-line completed"></div>
        <div className="progress-step completed">
          <div className="step-number">2</div>
          <span>Traveller Information</span>
        </div>
        <div className="progress-line completed"></div>
        <div className="progress-step active">
          <div className="step-number">3</div>
          <span>Payment</span>
        </div>
      </div>

      <div className="payment-container">
        <h2>Complete Your Payment</h2>

        {/* Error Banner */}
        {error && (
          <div className="error-banner">
            <span className="error-icon">⚠️</span>
            <span className="error-text">{error}</span>
          </div>
        )}

        <div className="payment-content">
          {/* Booking Summary */}
          <div className="booking-summary">
            <h3>Booking Summary</h3>
            
            <div className="summary-item">
              <span className="label">Booking Code:</span>
              <span className="value">{booking.bookingCode}</span>
            </div>
            
            <div className="summary-item">
              <span className="label">Status:</span>
              <span className={`value status-${booking.status.toLowerCase()}`}>
                {booking.status}
              </span>
            </div>
            
            <div className="summary-item">
              <span className="label">Created:</span>
              <span className="value">{formatDate(booking.createdAt)}</span>
            </div>
            
            <div className="summary-divider"></div>
            
            <div className="summary-item total">
              <span className="label">Total Amount:</span>
              <span className="value price">
                {formatPrice(booking.totalAmount)} {booking.currency}
              </span>
            </div>
          </div>

          {/* Payment Method Selection */}
          <div className="payment-method-section">
            <h3>Payment Method</h3>
            
            <div className="payment-methods">
              <label className={`payment-method-option ${paymentMethod === 'STRIPE' ? 'selected' : ''}`}>
                <input
                  type="radio"
                  name="paymentMethod"
                  value="STRIPE"
                  checked={paymentMethod === 'STRIPE'}
                  onChange={(e) => setPaymentMethod(e.target.value)}
                />
                <div className="method-info">
                  <span className="method-name">Credit/Debit Card</span>
                  <span className="method-desc">Powered by Stripe (Visa, Mastercard, Amex)</span>
                </div>
                <div className="method-icon">💳</div>
              </label>
              
              <label className="payment-method-option disabled">
                <input
                  type="radio"
                  name="paymentMethod"
                  value="VNPAY"
                  disabled
                />
                <div className="method-info">
                  <span className="method-name">VNPay</span>
                  <span className="method-desc">Vietnamese payment gateway (Coming soon)</span>
                </div>
                <div className="method-icon">🏦</div>
              </label>
              
              <label className="payment-method-option disabled">
                <input
                  type="radio"
                  name="paymentMethod"
                  value="MOMO"
                  disabled
                />
                <div className="method-info">
                  <span className="method-name">Momo</span>
                  <span className="method-desc">Vietnamese e-wallet (Coming soon)</span>
                </div>
                <div className="method-icon">📱</div>
              </label>
            </div>
          </div>

          {/* Terms and Conditions */}
          <div className="terms-section">
            <label className="terms-checkbox">
              <input
                type="checkbox"
                checked={acceptTerms}
                onChange={(e) => setAcceptTerms(e.target.checked)}
              />
              <span>
                I accept the <a href="/terms" target="_blank">Terms and Conditions</a> and <a href="/privacy" target="_blank">Privacy Policy</a>
              </span>
            </label>
          </div>

          {/* Payment Button */}
          <div className="payment-actions">
            <button
              onClick={handlePayment}
              disabled={processing || !acceptTerms}
              className="pay-button"
            >
              {processing ? (
                <>
                  <span className="spinner-small"></span>
                  Processing...
                </>
              ) : (
                <>
                  🔒 Pay {formatPrice(booking.totalAmount)} {booking.currency}
                </>
              )}
            </button>
            
            <button
              onClick={() => navigate(-1)}
              disabled={processing}
              className="back-link-button"
            >
              ← Back
            </button>
          </div>

          {/* Security Notice */}
          <div className="security-notice">
            <span className="security-icon">🔒</span>
            <span>Your payment is secure and encrypted. We do not store your card details.</span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Payment;

