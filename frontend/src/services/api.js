import axios from 'axios';
import { API_CONFIG } from '../config/api.config';

const api = axios.create({
  baseURL: API_CONFIG.BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: API_CONFIG.TIMEOUT,
});

// Request interceptor - Attach JWT token to all requests
api.interceptors.request.use(
  (config) => {
    // Retrieve token from localStorage
    const token = localStorage.getItem('token');
    
    if (token) {
      // Attach JWT token to Authorization header
      config.headers.Authorization = `Bearer ${token}`;
    }
    
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor
api.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    // Handle network errors
    if (!error.response) {
      console.error('Network Error:', error.message);
      error.message = 'Không thể kết nối đến server. Vui lòng kiểm tra kết nối mạng và đảm bảo backend đang chạy.';
      return Promise.reject(error);
    }

    // Handle HTTP errors
    const status = error.response.status;
    const data = error.response.data;

    if (status === 401) {
      // Unauthorized - clear authentication data
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      
      // Redirect to login if not already there
      if (window.location.pathname !== '/login' && window.location.pathname !== '/register') {
        window.location.href = '/login';
      }
      
      error.message = data?.message || 'Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.';
    } else if (status === 403) {
      // Forbidden - user doesn't have permission
      error.message = data?.message || 'Bạn không có quyền truy cập tài nguyên này.';
    } else if (status === 404) {
      error.message = data?.message || 'Không tìm thấy dữ liệu.';
    } else if (status === 409) {
      // Conflict - usually for "already exists" errors
      error.message = data?.message || 'Dữ liệu đã tồn tại.';
    } else if (status === 400) {
      // Validation errors
      if (typeof data === 'object' && data !== null) {
        // Handle validation errors from GlobalExceptionHandler
        if (data.status === 'VALIDATION_ERROR') {
          // Extract first validation error message
          const errorMessages = Object.entries(data)
            .filter(([key]) => key !== 'status')
            .map(([_, value]) => value);
          error.message = errorMessages.length > 0 ? errorMessages[0] : 'Dữ liệu không hợp lệ.';
          error.validationErrors = data; // Store full validation errors
        } else if (data.message) {
          // Handle runtime exception errors
          error.message = data.message;
        } else {
          // Fallback: try to extract any error message
          const errors = Object.values(data).filter(v => typeof v === 'string');
          error.message = errors.length > 0 ? errors[0] : 'Dữ liệu không hợp lệ.';
        }
      } else {
        error.message = data?.message || 'Dữ liệu không hợp lệ.';
      }
    } else if (status >= 500) {
      error.message = 'Lỗi server. Vui lòng thử lại sau.';
    } else {
      error.message = data?.message || error.message || 'Đã xảy ra lỗi.';
    }

    return Promise.reject(error);
  }
);

export const bookingService = {
  createBooking: (bookingData) => {
    return api.post('/bookings', bookingData);
  },
  
  getBookingById: (id) => {
    return api.get(`/bookings/${id}`);
  },
  
  getBookingByCode: (bookingCode) => {
    return api.get(`/bookings/code/${bookingCode}`);
  },
  
  getBookingsByUserId: (userId) => {
    return api.get(`/bookings/user/${userId}`);
  },
  
  updateBookingStatus: (id, status) => {
    return api.put(`/bookings/${id}/status`, { status });
  },
};

/**
 * Payment Service
 * 
 * Endpoints:
 * - POST /api/payments/create - Create payment for booking
 * - GET /api/payments/{id} - Get payment by ID
 * - GET /api/payments/booking/{bookingId} - Get payments for booking
 */
export const paymentService = {
  /**
   * Create payment for booking
   * POST /api/payments/create
   * 
   * @param {Object} paymentData - Payment request data
   * @param {string} paymentData.bookingId - Booking ID (required)
   * @param {string} paymentData.paymentMethod - Payment method: STRIPE, VNPAY, MOMO (default: STRIPE)
   * @param {string} paymentData.successUrl - Success redirect URL (optional)
   * @param {string} paymentData.cancelUrl - Cancel redirect URL (optional)
   * @returns {Promise} Response with payment details and checkout URL
   */
  createPayment: (paymentData) => {
    return api.post('/payments/create', paymentData);
  },
  
  /**
   * Get payment by ID
   * GET /api/payments/{id}
   * 
   * @param {string} id - Payment ID
   * @returns {Promise} Response with payment details
   */
  getPaymentById: (id) => {
    return api.get(`/payments/${id}`);
  },
  
  /**
   * Get all payments for a booking
   * GET /api/payments/booking/{bookingId}
   * 
   * @param {string} bookingId - Booking ID
   * @returns {Promise} Response with list of payments
   */
  getPaymentsByBookingId: (bookingId) => {
    return api.get(`/payments/booking/${bookingId}`);
  },
};

/**
 * Flight Service
 * 
 * Endpoints:
 * - GET /api/flights/search - Search flights with query params
 * - GET /api/flights/{id} - Get flight by ID
 * - GET /api/flights - Get all flights
 */
export const flightService = {
  /**
   * Search flights with query parameters
   * GET /api/flights/search?origin=SGN&destination=HAN&departureDate=2025-01-20&passengers=1&page=0&size=10
   * 
   * @param {Object} params - Search parameters
   * @param {string} params.origin - Origin airport code (required)
   * @param {string} params.destination - Destination airport code (required)
   * @param {string} params.departureDate - Departure date YYYY-MM-DD (required)
   * @param {number} params.passengers - Number of passengers (default: 1)
   * @param {number} params.minPrice - Minimum price filter (optional)
   * @param {number} params.maxPrice - Maximum price filter (optional)
   * @param {string} params.airline - Airline filter (optional)
   * @param {string} params.cabinClass - Cabin class: ECONOMY, BUSINESS, FIRST (optional)
   * @param {number} params.page - Page number (default: 0)
   * @param {number} params.size - Page size (default: 10)
   * @returns {Promise} Response with Page<FlightDTO>
   */
  searchFlights: (params) => {
    return api.get('/flights/search', { params });
  },
  
  /**
   * Get flight by ID
   * GET /api/flights/{id}
   * 
   * @param {string} id - Flight ID
   * @returns {Promise} Response with FlightDTO
   */
  getFlightById: (id) => {
    return api.get(`/flights/${id}`);
  },
  
  /**
   * Get all flights (for admin/testing)
   * GET /api/flights
   * 
   * @returns {Promise} Response with List<FlightDTO>
   */
  getAllFlights: () => {
    return api.get('/flights');
  },
};

/**
 * Authentication Service
 * 
 * Endpoints:
 * - POST /api/auth/register - Register new user
 * - POST /api/auth/login - Login and get JWT token
 */
export const authService = {
  /**
   * Register new user
   * POST /api/auth/register
   * Request: { email, password, phone, role }
   * Response: { id, email, phone, role, status, createdAt, updatedAt }
   */
  register: (userData) => {
    return api.post('/auth/register', userData);
  },
  
  /**
   * Login user
   * POST /api/auth/login
   * Request: { email, password }
   * Response: { accessToken: "eyJhbGciOiJIUzUxMiJ9..." }
   */
  login: (credentials) => {
    return api.post('/auth/login', credentials);
  },
};

/**
 * User Service
 * 
 * Protected endpoints (require JWT token)
 */
export const userService = {
  getUserById: (id) => {
    return api.get(`/users/${id}`);
  },
  
  getUserByEmail: (email) => {
    return api.get(`/users/email/${email}`);
  },
  
  getAllUsers: () => {
    return api.get('/users');
  },
  
  updateUser: (id, userData) => {
    return api.put(`/users/${id}`, userData);
  },
};

export const flightSegmentService = {
  searchFlights: (origin, destination) => {
    return api.get('/flight-segments/search', {
      params: { origin, destination }
    });
  },
  
  getSegmentsByBookingId: (bookingId) => {
    return api.get(`/flight-segments/booking/${bookingId}`);
  },
};

export const seatSelectionService = {
  createSeatSelection: (seatData) => {
    return api.post('/seat-selections', seatData);
  },
  
  getSeatSelectionsByPassengerId: (passengerId) => {
    return api.get(`/seat-selections/passenger/${passengerId}`);
  },
  
  getSeatSelectionsBySegmentId: (segmentId) => {
    return api.get(`/seat-selections/segment/${segmentId}`);
  },
};

export const baggageService = {
  createBaggageService: (passengerId, segmentId, weightKg, price) => {
    return api.post('/baggage-services', null, {
      params: { passengerId, segmentId, weightKg, price }
    });
  },
  
  getBaggageServicesByPassengerId: (passengerId) => {
    return api.get(`/baggage-services/passenger/${passengerId}`);
  },
  
  getBaggageServicesBySegmentId: (segmentId) => {
    return api.get(`/baggage-services/segment/${segmentId}`);
  },
};

export const ticketService = {
  getTicketsByBookingId: (bookingId) => {
    return api.get(`/tickets/booking/${bookingId}`);
  },
  
  getTicketById: (id) => {
    return api.get(`/tickets/${id}`);
  },
};

export const notificationService = {
  getNotificationsByUserId: (userId) => {
    return api.get(`/notifications/user/${userId}`);
  },
  
  markAsRead: (id) => {
    return api.put(`/notifications/${id}/read`);
  },
};

export default api;

