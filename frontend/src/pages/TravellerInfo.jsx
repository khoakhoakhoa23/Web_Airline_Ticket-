import { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { useBooking } from '../contexts/BookingContext';
import { toast } from 'react-toastify';
import '../styles/pages/TravellerInfo.css';

const TravellerInfo = () => {
  const { user } = useAuth();
  const { selectedFlight, updatePassengers, createBooking, loading, selectedSeats, seatPrice } = useBooking();
  const navigate = useNavigate();
  const location = useLocation();
  
  // Get number of passengers from state, selectedSeats, or default to 1
  const numPassengers = location.state?.passengers || 
                        (selectedSeats && selectedSeats.length > 0 ? selectedSeats.length : 1);
  
  // Initialize passengers array based on number
  const initializePassengers = () => {
    const passengers = [];
    for (let i = 0; i < numPassengers; i++) {
      passengers.push({
        title: 'MR',
        firstName: '',
        surname: '',
        dateOfBirth: '',
        gender: 'MALE',
        documentType: 'PASSPORT',
        documentNumber: '',
      });
    }
    return passengers;
  };
  
  const [formData, setFormData] = useState({
    email: user?.email || '',
    mobileNumber: user?.phone || '',
    receiveNewsletters: false,
    passengers: initializePassengers(),
    lostBaggageService: false,
  });
  const [submitting, setSubmitting] = useState(false);
  const [validationErrors, setValidationErrors] = useState({});

  // Check for selected flight on mount and restore from localStorage if needed
  useEffect(() => {
    if (!selectedFlight) {
      // Try to restore from localStorage
      const stored = localStorage.getItem('selectedFlight');
      if (stored) {
        try {
          const flight = JSON.parse(stored);
          if (flight) {
            // Flight exists in localStorage, continue
            return;
          }
        } catch (err) {
          console.error('Error parsing stored flight:', err);
        }
      }
      toast.error('No flight selected. Please select a flight first.');
      navigate('/flights');
    }
  }, [selectedFlight, navigate]);
  
  // Ensure passengers array matches number of passengers
  useEffect(() => {
    if (formData.passengers.length !== numPassengers) {
      const currentPassengers = formData.passengers;
      const newPassengers = [];
      
      for (let i = 0; i < numPassengers; i++) {
        if (currentPassengers[i]) {
          newPassengers.push(currentPassengers[i]);
        } else {
          newPassengers.push({
            title: 'MR',
            firstName: '',
            surname: '',
            dateOfBirth: '',
            gender: 'MALE',
            documentType: 'PASSPORT',
            documentNumber: '',
          });
        }
      }
      
      setFormData(prev => ({
        ...prev,
        passengers: newPassengers
      }));
    }
  }, [numPassengers]);

  // Restore saved passenger info if exists
  useEffect(() => {
    const saved = localStorage.getItem('passengerInfo');
    if (saved) {
      try {
        const savedData = JSON.parse(saved);
        setFormData(prev => ({
          ...prev,
          ...savedData,
          email: user?.email || savedData.email || '',
          mobileNumber: user?.phone || savedData.mobileNumber || '',
        }));
      } catch (err) {
        console.error('Error restoring passenger info:', err);
      }
    }
  }, [user]);
  
  // Restore seatPrice from localStorage if not in context
  useEffect(() => {
    if ((!seatPrice || seatPrice === 0) && selectedSeats && selectedSeats.length > 0) {
      const storedSeatPrice = localStorage.getItem('seatPrice');
      if (storedSeatPrice) {
        // seatPrice should be restored by BookingContext, but log for debugging
        console.log('Seat price from localStorage:', storedSeatPrice);
      }
    }
  }, [seatPrice, selectedSeats]);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    if (name.startsWith('passenger.')) {
      const [_, index, field] = name.split('.');
      const updatedPassengers = [...formData.passengers];
      updatedPassengers[parseInt(index)] = {
        ...updatedPassengers[parseInt(index)],
        [field]: value,
      };
      setFormData({ ...formData, passengers: updatedPassengers });
    } else {
      setFormData({
        ...formData,
        [name]: type === 'checkbox' ? checked : value,
      });
    }
  };

  /**
   * Validate form data with detailed error messages
   */
  const validateForm = () => {
    const errors = {};
    let isValid = true;

    // Validate email
    if (!formData.email || formData.email.trim() === '') {
      errors.email = 'Email is required';
      isValid = false;
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email.trim())) {
      errors.email = 'Please enter a valid email address';
      isValid = false;
    }

    // Validate mobile number
    if (!formData.mobileNumber || formData.mobileNumber.trim() === '') {
      errors.mobileNumber = 'Mobile number is required';
      isValid = false;
    } else if (!/^[0-9+\s()-]{8,20}$/.test(formData.mobileNumber.trim())) {
      errors.mobileNumber = 'Please enter a valid mobile number (8-20 digits)';
      isValid = false;
    }

    // Validate passengers
    for (let i = 0; i < formData.passengers.length; i++) {
      const passenger = formData.passengers[i];
      const passengerErrors = {};
      
      // Validate first name
      if (!passenger.firstName || passenger.firstName.trim() === '') {
        passengerErrors.firstName = 'First name is required';
        isValid = false;
      } else {
        const nameRegex = /^[a-zA-ZÀ-ỹ\s'-]{2,50}$/;
        if (!nameRegex.test(passenger.firstName.trim())) {
          passengerErrors.firstName = 'First name must be 2-50 characters and contain only letters, spaces, hyphens, and apostrophes';
          isValid = false;
        }
      }

      // Validate surname
      if (!passenger.surname || passenger.surname.trim() === '') {
        passengerErrors.surname = 'Surname is required';
        isValid = false;
      } else {
        const nameRegex = /^[a-zA-ZÀ-ỹ\s'-]{2,50}$/;
        if (!nameRegex.test(passenger.surname.trim())) {
          passengerErrors.surname = 'Surname must be 2-50 characters and contain only letters, spaces, hyphens, and apostrophes';
          isValid = false;
        }
      }

      // Validate date of birth
      if (!passenger.dateOfBirth) {
        passengerErrors.dateOfBirth = 'Date of birth is required';
        isValid = false;
      } else {
        const dob = new Date(passenger.dateOfBirth);
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        
        if (dob >= today) {
          passengerErrors.dateOfBirth = 'Date of birth must be in the past';
          isValid = false;
        } else {
          // Check if age is reasonable (at least 2 years old, not more than 120 years)
          const age = today.getFullYear() - dob.getFullYear();
          if (age < 2) {
            passengerErrors.dateOfBirth = 'Passenger must be at least 2 years old';
            isValid = false;
          } else if (age > 120) {
            passengerErrors.dateOfBirth = 'Please enter a valid date of birth';
            isValid = false;
          }
        }
      }

      // Validate document number
      if (!passenger.documentNumber || passenger.documentNumber.trim() === '') {
        passengerErrors.documentNumber = 'Document number is required';
        isValid = false;
      } else if (passenger.documentNumber.trim().length < 5) {
        passengerErrors.documentNumber = 'Document number must be at least 5 characters';
        isValid = false;
      }

      if (Object.keys(passengerErrors).length > 0) {
        errors[`passenger_${i}`] = passengerErrors;
      }
    }

    setValidationErrors(errors);
    
    if (!isValid) {
      // Show first error message
      const firstError = Object.values(errors)[0];
      if (typeof firstError === 'string') {
        toast.error(firstError);
      } else if (typeof firstError === 'object') {
        const firstFieldError = Object.values(firstError)[0];
        toast.error(firstFieldError);
      } else {
        toast.error('Please fill in all required fields correctly');
      }
    }

    return isValid;
  };

  /**
   * Handle form submission - Create booking
   */
  const handleContinue = async () => {
    // Clear previous validation errors
    setValidationErrors({});

    // Validate form
    if (!validateForm()) {
      return;
    }

    // Check if user is logged in
    if (!user || !user.id) {
      toast.error('Please login to continue');
      navigate('/login');
      return;
    }

    // Check if flight is selected
    const flightToUse = selectedFlight || (() => {
      const stored = localStorage.getItem('selectedFlight');
      return stored ? JSON.parse(stored) : null;
    })();
    
    if (!flightToUse) {
      toast.error('No flight selected. Please select a flight first.');
      navigate('/flights');
      return;
    }

    // Validate number of passengers matches seats (if seats were selected)
    if (selectedSeats && selectedSeats.length > 0 && formData.passengers.length !== selectedSeats.length) {
      toast.error(`Number of passengers (${formData.passengers.length}) must match number of selected seats (${selectedSeats.length})`);
      return;
    }

    setSubmitting(true);

    try {
      // Save passenger info to context
      updatePassengers(formData.passengers);

      // Get seatPrice from context or localStorage
      let finalSeatPrice = seatPrice || 0;
      if (!finalSeatPrice || finalSeatPrice === 0) {
        const storedSeatPrice = localStorage.getItem('seatPrice');
        if (storedSeatPrice) {
          finalSeatPrice = parseFloat(storedSeatPrice) || 0;
        }
      }
      
      // Get selected seats from context, location state, or localStorage
      let seatsToUse = selectedSeats || location.state?.selectedSeats || [];
      
      // Fallback to localStorage if context and state are empty
      if (!seatsToUse || seatsToUse.length === 0) {
        try {
          const storedSeats = localStorage.getItem('selectedSeats');
          if (storedSeats) {
            seatsToUse = JSON.parse(storedSeats);
            console.log('[TravellerInfo] Loaded seats from localStorage:', seatsToUse);
          }
        } catch (e) {
          console.error('[TravellerInfo] Error parsing stored seats:', e);
        }
      }
      
      console.log('[TravellerInfo] Selected seats from context:', selectedSeats);
      console.log('[TravellerInfo] Selected seats from location state:', location.state?.selectedSeats);
      console.log('[TravellerInfo] Final seatsToUse:', seatsToUse);
      
      // Helper function to get seat price
      const getPriceForSeat = (seatLabel) => {
        if (!seatLabel) return 0;
        const row = parseInt(seatLabel);
        // Front seats (rows 1-10) are premium
        if (row <= 10) return 500000;
        // Exit rows (usually rows 12-15) are premium
        if (row >= 12 && row <= 15) return 300000;
        // Regular seats (rows 11, 16+) have standard fee
        return 100000;
      };
      
      // Helper function to determine seat type
      const getSeatType = (seatLabel) => {
        if (!seatLabel) return 'STANDARD';
        const col = seatLabel.slice(-1); // Last character (A, B, C, D, E, F)
        if (col === 'A' || col === 'F') return 'WINDOW';
        if (col === 'C' || col === 'D') return 'AISLE';
        return 'MIDDLE';
      };
      
      // Prepare seat selections - map seats to passengers
      const seatSelections = seatsToUse.map((seatNumber, index) => {
        const price = getPriceForSeat(seatNumber);
        return {
          seatNumber: seatNumber,
          passengerIndex: index, // Map to passenger by index
          seatType: getSeatType(seatNumber),
          price: price // Ensure it's a number
        };
      });
      console.log('[TravellerInfo] Prepared seatSelections:', seatSelections);
      
      // ✅ Prepare booking data (userId removed - backend extracts from JWT token)
      // Ensure all numbers are properly formatted
      const bookingData = {
        // userId: user.id, // ✅ REMOVED - Backend extracts from JWT token (SecurityContext)
        currency: flightToUse.currency || 'VND',
        seatPrice: finalSeatPrice || 0, // Include seat price (will be converted to BigDecimal by Spring)
        flightSegments: [{
          airline: flightToUse.airline,
          flightNumber: flightToUse.flightNumber,
          origin: flightToUse.origin,
          destination: flightToUse.destination,
          departTime: flightToUse.departTime,
          arriveTime: flightToUse.arriveTime,
          cabinClass: flightToUse.cabinClass,
          baseFare: Number(flightToUse.baseFare) || 0,
          taxes: Number(flightToUse.taxes) || 0,
        }],
        passengers: formData.passengers.map((p, index) => ({
          fullName: `${p.firstName.trim()} ${p.surname.trim()}`,
          dateOfBirth: p.dateOfBirth,
          gender: p.gender,
          documentType: p.documentType,
          documentNumber: p.documentNumber.trim(),
        })),
        seatSelections: seatSelections, // ✅ Include seat selections
      };
      
      // ✅ Validate booking data before sending (userId validation removed)
      if (!bookingData.flightSegments || bookingData.flightSegments.length === 0) {
        throw new Error('Flight segments are missing');
      }
      if (!bookingData.passengers || bookingData.passengers.length === 0) {
        throw new Error('Passengers information is missing');
      }
      
      console.log('Creating booking with data:', {
        // userId removed - Backend extracts from JWT token
        currency: bookingData.currency,
        seatPrice: finalSeatPrice,
        flightNumber: bookingData.flightSegments[0].flightNumber,
        passengersCount: bookingData.passengers.length,
        seatSelectionsCount: seatSelections.length,
        seatSelections: seatSelections,
        totalExpected: ((Number(flightToUse.baseFare) || 0) + (Number(flightToUse.taxes) || 0) + finalSeatPrice)
      });
      console.log('✅ userId removed from request - Backend will extract from JWT token');
      console.log('✅ Seat selections included:', seatSelections);

      // Create booking
      const booking = await createBooking(bookingData);

      if (!booking || !booking.id) {
        throw new Error('Booking creation failed: No booking ID returned');
      }

      // Save traveller info to localStorage (for reference)
      localStorage.setItem('travellerInfo', JSON.stringify(formData));
      
      // Save booking ID for payment page
      localStorage.setItem('currentBookingId', booking.id);

      toast.success('Booking created successfully! Redirecting to payment...');

      // Small delay to show success message
      setTimeout(() => {
        // Navigate to payment page with booking ID
        navigate(`/booking/payment?booking_id=${booking.id}`, {
          replace: true
        });
      }, 500);
    } catch (err) {
      console.error('Error creating booking:', err);
      
      // Show detailed error message
      let errorMessage = 'Failed to create booking. Please try again.';
      if (err.response?.data?.message) {
        errorMessage = err.response.data.message;
      } else if (err.message) {
        errorMessage = err.message;
      }
      
      toast.error(errorMessage);
      
      // Don't navigate on error - let user fix and retry
    } finally {
      setSubmitting(false);
    }
  };

  // Get flight for display (from context or localStorage)
  const displayFlight = selectedFlight || (() => {
    const stored = localStorage.getItem('selectedFlight');
    return stored ? JSON.parse(stored) : null;
  })();

  if (!displayFlight) {
    return (
      <div className="traveller-info-page">
        <div className="loading-container">
          <p>Loading...</p>
        </div>
      </div>
    );
  }

  // Calculate total price: flight price + seat price
  const flightPrice = ((displayFlight.baseFare || 0) + (displayFlight.taxes || 0));
  // Get seatPrice from context or localStorage
  let displaySeatPrice = seatPrice || 0;
  if (!displaySeatPrice || displaySeatPrice === 0) {
    const storedSeatPrice = localStorage.getItem('seatPrice');
    if (storedSeatPrice) {
      displaySeatPrice = parseFloat(storedSeatPrice) || 0;
    }
  }
  const totalPrice = flightPrice + displaySeatPrice;

  return (
    <div className="traveller-info-page">
      <div className="progress-bar">
        <div className="progress-step completed">
          <div className="step-number">1</div>
          <span>Flight Section</span>
        </div>
        <div className="progress-line"></div>
        <div className="progress-step active">
          <div className="step-number">2</div>
          <span>Traveller information</span>
        </div>
        <div className="progress-line"></div>
        <div className="progress-step">
          <div className="step-number">3</div>
          <span>Payment</span>
        </div>
      </div>

      <div className="traveller-info-container">
        <div className="main-content">
          <a href="/booking/flight-selection" className="back-link">← Edit Traveler details</a>

          <div className="section">
            <h3>Contact information for all passengers</h3>
            <div className="form-row">
              <div className="form-group">
                <label>Email *</label>
                <input
                  type="email"
                  name="email"
                  value={formData.email}
                  onChange={handleChange}
                  placeholder="Email@yahoo.com"
                  required
                  className={validationErrors.email ? 'error' : ''}
                />
                {validationErrors.email && (
                  <span className="error-message">{validationErrors.email}</span>
                )}
              </div>
              <div className="form-group">
                <label>Mobile Number *</label>
                <div className="phone-input">
                  <input type="text" value="+84" readOnly className="country-code" />
                  <input
                    type="tel"
                    name="mobileNumber"
                    value={formData.mobileNumber}
                    onChange={handleChange}
                    placeholder="0912345678"
                    required
                    className={validationErrors.mobileNumber ? 'error' : ''}
                  />
                </div>
                {validationErrors.mobileNumber && (
                  <span className="error-message">{validationErrors.mobileNumber}</span>
                )}
              </div>
            </div>
            <label className="checkbox-label">
              <input
                type="checkbox"
                name="receiveNewsletters"
                checked={formData.receiveNewsletters}
                onChange={handleChange}
              />
              I do not wish to receive any newsletters about cheap air fares or other offers
            </label>
          </div>

          {formData.passengers.map((passenger, index) => (
            <div key={index} className="section">
              <h3>Passenger {index + 1}, adult</h3>
              <div className="form-row">
                <div className="form-group">
                  <label>Title</label>
                  <div className="radio-group">
                    <label>
                      <input
                        type="radio"
                        name={`passenger.${index}.title`}
                        value="MR"
                        checked={passenger.title === 'MR'}
                        onChange={handleChange}
                      />
                      Mr
                    </label>
                    <label>
                      <input
                        type="radio"
                        name={`passenger.${index}.title`}
                        value="MRS"
                        checked={passenger.title === 'MRS'}
                        onChange={handleChange}
                      />
                      Mrs/Ms
                    </label>
                  </div>
                </div>
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>First name *</label>
                  <input
                    type="text"
                    name={`passenger.${index}.firstName`}
                    value={passenger.firstName}
                    onChange={handleChange}
                    required
                    className={validationErrors[`passenger_${index}`]?.firstName ? 'error' : ''}
                  />
                  {validationErrors[`passenger_${index}`]?.firstName && (
                    <span className="error-message">{validationErrors[`passenger_${index}`].firstName}</span>
                  )}
                </div>
                <div className="form-group">
                  <label>Surname *</label>
                  <input
                    type="text"
                    name={`passenger.${index}.surname`}
                    value={passenger.surname}
                    onChange={handleChange}
                    required
                    className={validationErrors[`passenger_${index}`]?.surname ? 'error' : ''}
                  />
                  {validationErrors[`passenger_${index}`]?.surname && (
                    <span className="error-message">{validationErrors[`passenger_${index}`].surname}</span>
                  )}
                </div>
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>Date of Birth *</label>
                  <input
                    type="date"
                    name={`passenger.${index}.dateOfBirth`}
                    value={passenger.dateOfBirth}
                    onChange={handleChange}
                    required
                    max={new Date().toISOString().split('T')[0]}
                    className={validationErrors[`passenger_${index}`]?.dateOfBirth ? 'error' : ''}
                  />
                  {validationErrors[`passenger_${index}`]?.dateOfBirth && (
                    <span className="error-message">{validationErrors[`passenger_${index}`].dateOfBirth}</span>
                  )}
                </div>
                <div className="form-group">
                  <label>Gender *</label>
                  <select
                    name={`passenger.${index}.gender`}
                    value={passenger.gender}
                    onChange={handleChange}
                    required
                  >
                    <option value="MALE">Male</option>
                    <option value="FEMALE">Female</option>
                  </select>
                </div>
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>Document Type *</label>
                  <select
                    name={`passenger.${index}.documentType`}
                    value={passenger.documentType}
                    onChange={handleChange}
                    required
                  >
                    <option value="PASSPORT">Passport</option>
                    <option value="ID_CARD">ID Card</option>
                  </select>
                </div>
                <div className="form-group">
                  <label>Document Number *</label>
                  <input
                    type="text"
                    name={`passenger.${index}.documentNumber`}
                    value={passenger.documentNumber}
                    onChange={handleChange}
                    required
                    className={validationErrors[`passenger_${index}`]?.documentNumber ? 'error' : ''}
                  />
                  {validationErrors[`passenger_${index}`]?.documentNumber && (
                    <span className="error-message">{validationErrors[`passenger_${index}`].documentNumber}</span>
                  )}
                </div>
              </div>
            </div>
          ))}

          <div className="section">
            <h3>Hand Baggage</h3>
            <div className="baggage-info">
              <span className="baggage-icon">👜</span>
              <div>
                <p>Hand baggage</p>
                <p>1X7 Kg</p>
                <span className="included">Included</span>
              </div>
            </div>
          </div>

          <div className="section">
            <h3>Checked baggage</h3>
            <div className="baggage-info">
              <span className="baggage-icon">🧳</span>
              <div>
                <p>Checked baggage</p>
                <p>1X23 Kg</p>
                <span className="included">Included</span>
              </div>
            </div>
          </div>

          <div className="section">
            <h3>Lost Baggage Service</h3>
            <div className="service-card">
              <div className="service-image">📷</div>
              <div className="service-content">
                <p>
                  Our partner Blue Ribbon Bags helps you track lost baggage for up to 96 hours. 
                  If your bag is not returned by then you will receive 500 USD.
                </p>
                <p className="service-price">Only Au$ 10.86</p>
                <div className="radio-group">
                  <label>
                    <input
                      type="radio"
                      name="lostBaggageService"
                      value="false"
                      checked={!formData.lostBaggageService}
                      onChange={(e) => setFormData({...formData, lostBaggageService: false})}
                    />
                    No
                  </label>
                  <label>
                    <input
                      type="radio"
                      name="lostBaggageService"
                      value="true"
                      checked={formData.lostBaggageService}
                      onChange={(e) => setFormData({...formData, lostBaggageService: true})}
                    />
                    Yes
                  </label>
                </div>
              </div>
            </div>
          </div>

          <p className="confirmation-text">
            By booking you confirm that the names on the booking match those on the passports of those travelling.
          </p>

          <button 
            onClick={handleContinue} 
            className="continue-button"
            disabled={submitting || loading}
          >
            {submitting || loading ? (
              <>
                <span className="spinner-small"></span>
                Creating Booking...
              </>
            ) : (
              'Continue to Payment'
            )}
          </button>
        </div>

        <div className="order-summary">
          <h3>Your Order</h3>
          <div className="order-section">
            <div className="order-item">
              <span className="order-icon">✈</span>
              <div>
                <p><strong>Departure</strong></p>
                <p>{new Date(displayFlight.departTime).toLocaleDateString('en-US', { weekday: 'short', year: 'numeric', month: 'short', day: 'numeric' })}</p>
                <p>{new Date(displayFlight.departTime).toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })} - {new Date(displayFlight.arriveTime).toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })}</p>
                <p>{displayFlight.origin} - {displayFlight.destination}</p>
              </div>
            </div>
          </div>
          <div className="order-section">
            <p><strong>Bags</strong></p>
            <p>👜 Hand baggage: 1X7 Kg</p>
            <p>🧳 Checked baggage: 1X23 Kg</p>
          </div>
          <div className="order-section">
            <p><strong>Price Breakdown</strong></p>
            <p>Flight: {new Intl.NumberFormat('vi-VN').format(flightPrice)} {displayFlight.currency || 'VND'}</p>
            {displaySeatPrice > 0 && (
              <p>Seat Selection: +{new Intl.NumberFormat('vi-VN').format(displaySeatPrice)} {displayFlight.currency || 'VND'}</p>
            )}
          </div>
          <div className="order-total">
            <p>Total: <strong>{new Intl.NumberFormat('vi-VN').format(totalPrice)} {displayFlight.currency || 'VND'}</strong></p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default TravellerInfo;

