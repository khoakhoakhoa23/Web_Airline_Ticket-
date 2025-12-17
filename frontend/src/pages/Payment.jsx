import { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { bookingService, paymentService } from '../services/api';
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
 * 4. Create payment via API
 * 5. Redirect to payment provider (Stripe Checkout)
 */
const Payment = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  
  // State
  const [booking, setBooking] = useState(null);
  const [paymentMethod, setPaymentMethod] = useState('STRIPE');
  const [loading, setLoading] = useState(true);
  const [processing, setProcessing] = useState(false);
  const [error, setError] = useState('');

  /**
   * Fetch booking details on page load
   */
  useEffect(() => {
    const fetchBooking = async () => {
      try {
        // Get booking ID from URL params or localStorage
        let bookingId = searchParams.get('booking_id');
        
        if (!bookingId) {
          // Try to get from localStorage (temporary solution)
          const storedBookingId = localStorage.getItem('currentBookingId');
          if (storedBookingId) {
            bookingId = storedBookingId;
          } else {
            setError('No booking ID found. Please create a booking first.');
            setLoading(false);
            return;
          }
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
  }, [searchParams, navigate]);

  const handleBillingChange = (e) => {
    const { name, value } = e.target;
    setBillingAddress({
      ...billingAddress,
      [name]: value,
    });
  };

  const calculateTotal = () => {
    if (!selectedFlight) return 0;
    const basePrice = ((selectedFlight.baseFare || 0) + (selectedFlight.taxes || 0));
    const supportPrice = extraServices?.supportPackage === 'STANDARD' ? 56.93 : extraServices?.supportPackage === 'PLATINUM' ? 58.49 : 0;
    const medicalPrice = extraServices?.medicalCover ? 70.86 : 0;
    const collapsePrice = extraServices?.collapseCover ? 18.86 : 0;
    const baggagePrice = travellerInfo?.lostBaggageService ? 10.86 : 0;
    return basePrice + supportPrice + medicalPrice + collapsePrice + baggagePrice;
  };

  const handlePayment = async () => {
    if (!acceptedTerms) {
      setError('Please accept the terms and conditions');
      return;
    }

    if (!user) {
      navigate('/login');
      return;
    }

    setLoading(true);
    setError('');

    try {
      // Prepare flight segment data (remove id and bookingId if present)
      const flightSegmentData = {
        airline: selectedFlight.airline,
        flightNumber: selectedFlight.flightNumber,
        origin: selectedFlight.origin,
        destination: selectedFlight.destination,
        departTime: selectedFlight.departTime,
        arriveTime: selectedFlight.arriveTime,
        cabinClass: selectedFlight.cabinClass,
        baseFare: selectedFlight.baseFare || 0,
        taxes: selectedFlight.taxes || 0,
      };

      // Create booking
      const bookingData = {
        userId: user.id,
        currency: 'AUD',
        flightSegments: [flightSegmentData],
        passengers: travellerInfo.passengers.map(p => ({
          fullName: `${p.firstName} ${p.surname}`,
          dateOfBirth: p.dateOfBirth,
          gender: p.gender,
          documentType: p.documentType,
          documentNumber: p.documentNumber,
        })),
      };

      const bookingResponse = await bookingService.createBooking(bookingData);
      const booking = bookingResponse.data;

      // Create payment
      const paymentData = {
        bookingId: booking.id,
        provider: paymentMethod,
        amount: calculateTotal(),
        transactionId: `TXN${Date.now()}`,
      };

      const paymentResponse = await paymentService.createPayment(paymentData);
      
      // Clear local storage
      localStorage.removeItem('selectedFlight');
      localStorage.removeItem('travellerInfo');
      localStorage.removeItem('extraServices');
      localStorage.removeItem('flightSearch');

      // Navigate to confirmation
      navigate(`/booking/confirmation/${booking.id}`);
    } catch (err) {
      setError(err.message || err.response?.data?.message || 'Payment failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  if (!selectedFlight || !travellerInfo) {
    return <div>Loading...</div>;
  }

  const total = calculateTotal();

  return (
    <div className="payment-page">
      <div className="progress-bar">
        <div className="progress-step completed">
          <div className="step-number">1</div>
          <span>Flight Section</span>
        </div>
        <div className="progress-line"></div>
        <div className="progress-step completed">
          <div className="step-number">2</div>
          <span>Traveller information</span>
        </div>
        <div className="progress-line"></div>
        <div className="progress-step active">
          <div className="step-number">3</div>
          <span>Payment</span>
        </div>
      </div>

      <div className="payment-container">
        <div className="main-content">
          <h2>Choose payment method</h2>

          <div className="payment-methods">
            <div className="payment-method">
              <label>
                <input
                  type="radio"
                  name="paymentMethod"
                  value="CARD"
                  checked={paymentMethod === 'CARD'}
                  onChange={(e) => setPaymentMethod(e.target.value)}
                />
                <div className="payment-method-content">
                  <div>
                    <strong>Debit card/ credit card</strong>
                    <p>Up to -AU$ 25.36 discount.</p>
                  </div>
                  <div className="payment-logos">
                    <span>VISA</span>
                    <span>Mastercard</span>
                    <span>+ More</span>
                  </div>
                </div>
              </label>
            </div>

            <div className="payment-method">
              <label>
                <input
                  type="radio"
                  name="paymentMethod"
                  value="PAYPAL"
                  checked={paymentMethod === 'PAYPAL'}
                  onChange={(e) => setPaymentMethod(e.target.value)}
                />
                <div className="payment-method-content">
                  <div>
                    <strong>PayPal</strong>
                    <p>You will be redirected to PayPal's login page, where you can log in to your account and complete the booking.</p>
                  </div>
                  <div className="payment-logos">
                    <span>PayPal</span>
                  </div>
                </div>
              </label>
            </div>
          </div>

          {paymentMethod === 'PAYPAL' && (
            <div className="billing-address">
              <h3>Billing Address</h3>
              <div className="form-row">
                <div className="form-group">
                  <label>First name *</label>
                  <input
                    type="text"
                    name="firstName"
                    value={billingAddress.firstName}
                    onChange={handleBillingChange}
                    required
                  />
                </div>
                <div className="form-group">
                  <label>Surname *</label>
                  <input
                    type="text"
                    name="surname"
                    value={billingAddress.surname}
                    onChange={handleBillingChange}
                    required
                  />
                </div>
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>Street *</label>
                  <input
                    type="text"
                    name="street"
                    value={billingAddress.street}
                    onChange={handleBillingChange}
                    required
                  />
                </div>
                <div className="form-group">
                  <label>Postcode *</label>
                  <input
                    type="text"
                    name="postcode"
                    value={billingAddress.postcode}
                    onChange={handleBillingChange}
                    required
                  />
                </div>
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>City *</label>
                  <input
                    type="text"
                    name="city"
                    value={billingAddress.city}
                    onChange={handleBillingChange}
                    required
                  />
                </div>
                <div className="form-group">
                  <label>Country *</label>
                  <input
                    type="text"
                    name="country"
                    value={billingAddress.country}
                    onChange={handleBillingChange}
                    required
                  />
                </div>
              </div>
            </div>
          )}

          <div className="total-amount">
            <div className="amount-row">
              <span>Subtotal</span>
              <span>AU$ {total.toLocaleString()}</span>
            </div>
            <div className="amount-row total">
              <span>Amount to pay</span>
              <span>AU$ {total.toLocaleString()}</span>
            </div>
          </div>

          {error && <div className="error-message">{error}</div>}

          <div className="terms-section">
            <label className="checkbox-label">
              <input
                type="checkbox"
                checked={acceptedTerms}
                onChange={(e) => setAcceptedTerms(e.target.checked)}
                required
              />
              I have read and accept Flight Networks travel conditions, Fare Rules, the airline's general terms and conditions, 
              and I have verified that I have entered my booking information correctly.
            </label>
            <p className="terms-links">
              You can read our <a href="/privacy">Privacy policy</a> here. Additional airline general terms and conditions is available here: 
              <a href="/terms/virgin"> Virgin Australia</a>, <a href="/terms/qantas">Qantas Airways</a>.
            </p>
          </div>

          <button
            onClick={handlePayment}
            className="pay-button"
            disabled={loading || !acceptedTerms}
          >
            {loading ? 'Processing...' : 'Pay'}
          </button>
        </div>

        <div className="order-summary">
          <h3>Your Order</h3>
          <div className="order-section">
            <div className="order-item">
              <span className="order-icon">✈</span>
              <div>
                <p><strong>Departure</strong></p>
                <p>{new Date(selectedFlight.departTime).toLocaleDateString('en-US', { weekday: 'short', year: 'numeric', month: 'short', day: 'numeric' })}</p>
                <p>{new Date(selectedFlight.departTime).toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })} - {new Date(selectedFlight.arriveTime).toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })}</p>
                <p>{selectedFlight.origin} - {selectedFlight.destination}</p>
              </div>
            </div>
          </div>
          <div className="order-section">
            <p><strong>Bags</strong></p>
            <p>👜 Hand baggage: 1X7 Kg</p>
            <p>🧳 Checked baggage: 1X23 Kg</p>
          </div>
          <div className="order-total">
            <p>Total: <strong>AU${total.toLocaleString()}</strong></p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Payment;

