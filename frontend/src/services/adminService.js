import api from './api';

/**
 * Admin Service
 * 
 * Handles all admin-related API calls
 * All methods require ADMIN role
 */

// ==================== DASHBOARD ====================

export const getDashboardStats = async () => {
  const response = await api.get('/admin/dashboard');
  return response.data;
};

// ==================== BOOKINGS ====================

export const getAllBookings = async (page = 0, size = 20, status = null, sortBy = 'createdAt') => {
  const params = { page, size, sortBy };
  if (status) params.status = status;
  
  const response = await api.get('/admin/bookings', { params });
  return response.data;
};

export const getBookingById = async (id) => {
  const response = await api.get(`/admin/bookings/${id}`);
  return response.data;
};

export const approveBooking = async (id) => {
  const response = await api.put(`/admin/bookings/${id}/approve`);
  return response.data;
};

export const cancelBooking = async (id) => {
  const response = await api.put(`/admin/bookings/${id}/cancel`);
  return response.data;
};

// ==================== USERS ====================

export const getAllUsers = async (page = 0, size = 20) => {
  const params = { page, size };
  const response = await api.get('/admin/users', { params });
  return response.data;
};

export const getUserById = async (id) => {
  const response = await api.get(`/admin/users/${id}`);
  return response.data;
};

export const updateUserRole = async (id, role) => {
  const response = await api.put(`/admin/users/${id}/role`, { role });
  return response.data;
};

export const updateUserStatus = async (id, status) => {
  const response = await api.put(`/admin/users/${id}/status`, { status });
  return response.data;
};

// ==================== FLIGHTS ====================

export const getAllFlights = async (page = 0, size = 20) => {
  const params = { page, size };
  const response = await api.get('/admin/flights', { params });
  return response.data;
};

export const createFlight = async (flightData) => {
  const response = await api.post('/admin/flights', flightData);
  return response.data;
};

export const updateFlight = async (id, flightData) => {
  const response = await api.put(`/admin/flights/${id}`, flightData);
  return response.data;
};

export const deleteFlight = async (id) => {
  const response = await api.delete(`/admin/flights/${id}`);
  return response.data;
};

