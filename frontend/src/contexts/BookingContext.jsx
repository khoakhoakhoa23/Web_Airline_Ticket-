import { createContext, useContext, useState, useEffect } from 'react';
import axios from 'axios';
import { toast } from 'react-toastify';

const BookingContext = createContext(null);

export const BookingProvider = ({ children }) => {
  // Booking state
  const [selectedFlight, setSelectedFlight] = useState(null);
  const [selectedSeats, setSelectedSeats] = useState([]);
  const [seatPrice, setSeatPrice] = useState(0);
  const [passengers, setPassengers] = useState([]);
  const [extraServices, setExtraServices] = useState(null);
  const [currentBooking, setCurrentBooking] = useState(null);
  const [loading, setLoading] = useState(false);
  
  // Restore booking state on mount
  useEffect(() => {
    restoreBookingState();
  }, []);

  /**
   * Select flight for booking
   */
  const selectFlight = (flight) => {
    setSelectedFlight(flight);
    // Save to localStorage for persistence
    localStorage.setItem('selectedFlight', JSON.stringify(flight));
  };

  /**
   * Select seats
   */
  const selectSeats = (seats, price) => {
    setSelectedSeats(seats);
    setSeatPrice(price || 0);
    // Save to localStorage
    localStorage.setItem('selectedSeats', JSON.stringify(seats));
    localStorage.setItem('seatPrice', price || 0);
  };

  /**
   * Update passengers
   */
  const updatePassengers = (passengerList) => {
    setPassengers(passengerList);
    // Save to localStorage
    localStorage.setItem('passengers', JSON.stringify(passengerList));
  };

  /**
   * Update extra services
   */
  const updateExtraServices = (services) => {
    setExtraServices(services);
    // Save to localStorage
    localStorage.setItem('extraServices', JSON.stringify(services));
  };

  /**
   * Calculate total price
   */
  const calculateTotal = () => {
    let total = 0;

    // Flight price
    if (selectedFlight) {
      const flightPrice = (selectedFlight.baseFare || 0) + (selectedFlight.taxes || 0);
      total += flightPrice * (passengers.length || 1);
    }

    // Seat price
    total += seatPrice || 0;

    // Extra services
    if (extraServices) {
      const supportPrice = extraServices.supportPackage === 'STANDARD' ? 56.93 : 58.49;
      total += supportPrice;

      if (extraServices.medicalCover) total += 70.86;
      if (extraServices.collapseCover) total += 18.86;
    }

    return total;
  };

  /**
   * Create booking via API
   */
  const createBooking = async (bookingData) => {
    setLoading(true);
    try {
      const token = localStorage.getItem('token');
      if (!token) {
        throw new Error('Please login to create booking');
      }

      // ✅ Validate required fields (userId removed - backend will get from JWT token)
      if (!bookingData.flightSegments || bookingData.flightSegments.length === 0) {
        throw new Error('At least one flight segment is required');
      }
      if (!bookingData.passengers || bookingData.passengers.length === 0) {
        throw new Error('At least one passenger is required');
      }

      // ✅ Remove userId from request body - Backend will extract from JWT token
      const { userId, ...bookingPayload } = bookingData;

      console.log('Creating booking with data:', {
        currency: bookingPayload.currency,
        seatPrice: bookingPayload.seatPrice,
        flightSegmentsCount: bookingPayload.flightSegments.length,
        passengersCount: bookingPayload.passengers.length
      });
      console.log('✅ userId removed from request - Backend will extract from JWT token');

      const response = await axios.post(
        'http://localhost:8080/api/bookings',
        bookingPayload, // ✅ Send without userId
        {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          },
          timeout: 30000 // 30 seconds timeout
        }
      );

      const booking = response.data;
      
      if (!booking || !booking.id) {
        throw new Error('Invalid booking response from server');
      }
      
      setCurrentBooking(booking);

      // Save booking to localStorage
      localStorage.setItem('currentBooking', JSON.stringify(booking));
      localStorage.setItem('currentBookingId', booking.id);

      console.log('Booking created successfully:', {
        id: booking.id,
        bookingCode: booking.bookingCode,
        status: booking.status,
        totalAmount: booking.totalAmount
      });
      
      return booking;

    } catch (error) {
      console.error('Error creating booking:', error);
      
      let errorMessage = 'Failed to create booking';
      
      if (error.response) {
        // Server responded with error
        const status = error.response.status;
        const data = error.response.data;
        
        // ✅ CRITICAL: Always use backend message if available
        if (data?.message) {
          errorMessage = data.message;
        } else if (status === 400) {
          // Validation error - try to extract field errors
          if (typeof data === 'object' && data !== null) {
            // Extract first error message from validation errors
            const errors = Object.values(data).filter(v => typeof v === 'string');
            errorMessage = errors.length > 0 ? errors[0] : 'Invalid booking data. Please check your information.';
          } else {
            errorMessage = 'Invalid booking data. Please check your information.';
          }
        } else if (status === 401) {
          errorMessage = data?.message || 'Please login to create booking';
        } else if (status === 403) {
          errorMessage = data?.message || 'You do not have permission to create this booking';
        } else if (status === 404) {
          // ✅ Use backend message for 404 (could be user not found, flight not found, etc.)
          errorMessage = data?.message || 'Resource not found. Please try again.';
        } else if (status === 409) {
          errorMessage = data?.message || 'This record already exists. Please try again.';
        } else if (status >= 500) {
          errorMessage = data?.message || 'Server error. Please try again later.';
        }
      } else if (error.request) {
        // Request was made but no response received
        errorMessage = 'Cannot connect to server. Please check your internet connection.';
      } else if (error.message) {
        errorMessage = error.message;
      }

      toast.error(errorMessage);
      throw new Error(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  /**
   * Get booking by ID
   */
  const getBookingById = async (bookingId) => {
    setLoading(true);
    try {
      const token = localStorage.getItem('token');
      if (!token) {
        throw new Error('Please login to view booking');
      }

      const response = await axios.get(
        `http://localhost:8080/api/bookings/${bookingId}`,
        {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        }
      );

      const booking = response.data;
      setCurrentBooking(booking);
      return booking;

    } catch (error) {
      console.error('Error fetching booking:', error);
      toast.error('Failed to load booking');
      throw error;
    } finally {
      setLoading(false);
    }
  };

  /**
   * Reset booking state (clear all data)
   */
  const resetBooking = () => {
    setSelectedFlight(null);
    setSelectedSeats([]);
    setSeatPrice(0);
    setPassengers([]);
    setExtraServices(null);
    setCurrentBooking(null);

    // Clear localStorage
    localStorage.removeItem('selectedFlight');
    localStorage.removeItem('selectedSeats');
    localStorage.removeItem('seatPrice');
    localStorage.removeItem('passengers');
    localStorage.removeItem('extraServices');
    localStorage.removeItem('currentBooking');
    localStorage.removeItem('travellerInfo');
  };

  /**
   * Restore booking state from localStorage (on page refresh)
   */
  const restoreBookingState = () => {
    try {
      const storedFlight = localStorage.getItem('selectedFlight');
      const storedSeats = localStorage.getItem('selectedSeats');
      const storedSeatPrice = localStorage.getItem('seatPrice');
      const storedPassengers = localStorage.getItem('passengers');
      const storedServices = localStorage.getItem('extraServices');
      const storedBooking = localStorage.getItem('currentBooking');

      if (storedFlight) setSelectedFlight(JSON.parse(storedFlight));
      if (storedSeats) setSelectedSeats(JSON.parse(storedSeats));
      if (storedSeatPrice) setSeatPrice(parseFloat(storedSeatPrice));
      if (storedPassengers) setPassengers(JSON.parse(storedPassengers));
      if (storedServices) setExtraServices(JSON.parse(storedServices));
      if (storedBooking) setCurrentBooking(JSON.parse(storedBooking));
    } catch (error) {
      console.error('Error restoring booking state:', error);
    }
  };

  const value = {
    // State
    selectedFlight,
    selectedSeats,
    seatPrice,
    passengers,
    extraServices,
    currentBooking,
    loading,

    // Actions
    selectFlight,
    selectSeats,
    updatePassengers,
    updateExtraServices,
    calculateTotal,
    createBooking,
    getBookingById,
    resetBooking,
    restoreBookingState,
  };

  return (
    <BookingContext.Provider value={value}>
      {children}
    </BookingContext.Provider>
  );
};

export const useBooking = () => {
  const context = useContext(BookingContext);
  if (!context) {
    throw new Error('useBooking must be used within BookingProvider');
  }
  return context;
};
