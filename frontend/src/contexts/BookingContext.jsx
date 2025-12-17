import { createContext, useContext, useState, useCallback } from 'react';
import { bookingService, paymentService } from '../services/api';
import { toast } from 'react-toastify';

/**
 * Booking Context
 * 
 * Manages booking flow state:
 * - Selected flight
 * - Passenger information
 * - Booking details
 * - Payment status
 */

const BookingContext = createContext();

export const BookingProvider = ({ children }) => {
  // Selected flight
  const [selectedFlight, setSelectedFlight] = useState(() => {
    const saved = localStorage.getItem('selectedFlight');
    return saved ? JSON.parse(saved) : null;
  });

  // Passenger information
  const [passengers, setPassengers] = useState(() => {
    const saved = localStorage.getItem('passengerInfo');
    return saved ? JSON.parse(saved) : [];
  });

  // Current booking
  const [currentBooking, setCurrentBooking] = useState(() => {
    const saved = localStorage.getItem('currentBooking');
    return saved ? JSON.parse(saved) : null;
  });

  // Payment info
  const [paymentInfo, setPaymentInfo] = useState(() => {
    const saved = localStorage.getItem('paymentInfo');
    return saved ? JSON.parse(saved) : null;
  });

  // Loading states
  const [loading, setLoading] = useState(false);
  const [processingPayment, setProcessingPayment] = useState(false);

  /**
   * Select flight for booking
   */
  const selectFlight = useCallback((flight) => {
    setSelectedFlight(flight);
    localStorage.setItem('selectedFlight', JSON.stringify(flight));
    toast.success('Flight selected');
  }, []);

  /**
   * Update passenger information
   */
  const updatePassengers = useCallback((passengerData) => {
    setPassengers(passengerData);
    localStorage.setItem('passengerInfo', JSON.stringify(passengerData));
  }, []);

  /**
   * Create booking
   */
  const createBooking = useCallback(async (bookingData) => {
    setLoading(true);

    try {
      const response = await bookingService.createBooking(bookingData);
      const booking = response.data;

      setCurrentBooking(booking);
      localStorage.setItem('currentBooking', JSON.stringify(booking));
      localStorage.setItem('currentBookingId', booking.id);

      toast.success('Booking created successfully!');
      return booking;
    } catch (error) {
      console.error('Error creating booking:', error);
      const message = error.response?.data?.message || 'Failed to create booking. Please try again.';
      toast.error(message);
      throw error;
    } finally {
      setLoading(false);
    }
  }, []);

  /**
   * Create payment
   */
  const createPayment = useCallback(async (paymentData) => {
    setProcessingPayment(true);

    try {
      const response = await paymentService.createPayment(paymentData);
      const payment = response.data;

      setPaymentInfo(payment);
      localStorage.setItem('paymentInfo', JSON.stringify(payment));

      return payment;
    } catch (error) {
      console.error('Error creating payment:', error);
      const message = error.response?.data?.message || 'Failed to create payment. Please try again.';
      toast.error(message);
      throw error;
    } finally {
      setProcessingPayment(false);
    }
  }, []);

  /**
   * Get booking by ID
   */
  const fetchBooking = useCallback(async (bookingId) => {
    setLoading(true);

    try {
      const response = await bookingService.getBookingById(bookingId);
      const booking = response.data;

      setCurrentBooking(booking);
      localStorage.setItem('currentBooking', JSON.stringify(booking));

      return booking;
    } catch (error) {
      console.error('Error fetching booking:', error);
      const message = error.response?.data?.message || 'Failed to load booking details.';
      toast.error(message);
      throw error;
    } finally {
      setLoading(false);
    }
  }, []);

  /**
   * Clear booking data (after completion or cancellation)
   */
  const clearBooking = useCallback(() => {
    setSelectedFlight(null);
    setPassengers([]);
    setCurrentBooking(null);
    setPaymentInfo(null);
    
    localStorage.removeItem('selectedFlight');
    localStorage.removeItem('passengerInfo');
    localStorage.removeItem('currentBooking');
    localStorage.removeItem('currentBookingId');
    localStorage.removeItem('paymentInfo');
  }, []);

  /**
   * Check if booking flow can proceed
   */
  const canProceedToPayment = useCallback(() => {
    return selectedFlight && passengers.length > 0 && currentBooking;
  }, [selectedFlight, passengers, currentBooking]);

  const value = {
    // State
    selectedFlight,
    passengers,
    currentBooking,
    paymentInfo,
    loading,
    processingPayment,
    
    // Actions
    selectFlight,
    updatePassengers,
    createBooking,
    createPayment,
    fetchBooking,
    clearBooking,
    canProceedToPayment,
  };

  return (
    <BookingContext.Provider value={value}>
      {children}
    </BookingContext.Provider>
  );
};

/**
 * Custom hook to use booking context
 */
export const useBooking = () => {
  const context = useContext(BookingContext);
  if (!context) {
    throw new Error('useBooking must be used within BookingProvider');
  }
  return context;
};

