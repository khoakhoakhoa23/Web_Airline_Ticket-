// Utility để test API connection
import api from '../services/api';

export const testApiConnection = async () => {
  try {
    // Test basic connection
    const response = await api.get('/users');
    console.log('✅ API Connection successful!', response.data);
    return { success: true, data: response.data };
  } catch (error) {
    console.error('❌ API Connection failed:', error.message);
    if (error.response) {
      console.error('Response status:', error.response.status);
      console.error('Response data:', error.response.data);
    }
    return { success: false, error: error.message };
  }
};

// Test login endpoint
export const testLogin = async (email, password) => {
  try {
    const response = await api.post('/users/login', { email, password });
    console.log('✅ Login test successful!', response.data);
    return { success: true, data: response.data };
  } catch (error) {
    console.error('❌ Login test failed:', error.message);
    return { success: false, error: error.message };
  }
};

// Test flight search
export const testFlightSearch = async (origin, destination) => {
  try {
    const response = await api.get('/flight-segments/search', {
      params: { origin, destination }
    });
    console.log('✅ Flight search test successful!', response.data);
    return { success: true, data: response.data };
  } catch (error) {
    console.error('❌ Flight search test failed:', error.message);
    return { success: false, error: error.message };
  }
};

