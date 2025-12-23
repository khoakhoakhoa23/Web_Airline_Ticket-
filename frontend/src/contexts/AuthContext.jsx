import { createContext, useContext, useState, useEffect } from 'react';
import { authService, userService } from '../services/api';
import { jwtDecode } from 'jwt-decode';

const AuthContext = createContext(null);

/**
 * AuthProvider - Manages authentication state for the application
 * 
 * Features:
 * - Login with email/password → Get JWT accessToken
 * - Register user → Auto-login
 * - Store accessToken in localStorage
 * - Decode JWT to get user info (userId, email, role)
 * - Auto-load user from token on app startup
 * - Logout (clear token)
 * 
 * Security:
 * - Password never stored in frontend
 * - Token stored in localStorage
 * - Token automatically attached to all API calls via Axios interceptor
 */
export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  /**
   * Decode JWT token to extract user information
   * 
   * JWT payload contains:
   * - sub: userId
   * - email: user's email
   * - role: user's role (USER, ADMIN)
   * - iat: issued at timestamp
   * - exp: expiration timestamp
   */
  const decodeToken = (token) => {
    try {
      const decoded = jwtDecode(token);
      return {
        id: decoded.sub,          // userId from subject
        email: decoded.email,
        role: decoded.role,
        exp: decoded.exp,         // Token expiration
      };
    } catch (error) {
      console.error('Error decoding token:', error);
      return null;
    }
  };

  /**
   * Check if token is expired
   */
  const isTokenExpired = (token) => {
    try {
      const decoded = jwtDecode(token);
      const currentTime = Date.now() / 1000; // Convert to seconds
      return decoded.exp < currentTime;
    } catch (error) {
      return true;
    }
  };

  // Initialize: Load user from token on mount
  // FIX: Restore user authentication state immediately on page load/refresh
  // This prevents the "logout on F5" issue where ProtectedRoute redirects
  // before token is restored from localStorage
  useEffect(() => {
    const restoreAuth = () => {
      const storedToken = localStorage.getItem('token');
      
      if (!storedToken) {
        // No token found - user is not authenticated
        setLoading(false);
        return;
      }
      
      // Check if token is expired
      if (isTokenExpired(storedToken)) {
        console.log('Token expired, clearing authentication');
        localStorage.removeItem('token');
        setUser(null);
        setLoading(false);
        return;
      }
      
      // Decode token to get user info
      // This is synchronous - no API call needed
      const userData = decodeToken(storedToken);
      if (userData) {
        // ✅ Immediately set user state - no async operation needed
        // This ensures user is authenticated before ProtectedRoute checks
        setUser(userData);
        console.log('User authentication restored from token');
      } else {
        // Invalid token - remove it
        console.warn('Invalid token format, clearing authentication');
        localStorage.removeItem('token');
        setUser(null);
      }
      
      setLoading(false);
    };

    // Restore authentication immediately
    restoreAuth();
  }, []);

  /**
   * Login user with email and password
   * 
   * Flow:
   * 1. Call POST /api/auth/login with { email, password }
   * 2. Backend returns: { accessToken: "eyJhbGciOiJIUzUxMiJ9..." }
   * 3. Store accessToken in localStorage
   * 4. Decode token to get user info (id, email, role)
   * 5. Set user state
   * 
   * @param email User's email
   * @param password User's password
   * @returns User object { id, email, role, exp }
   * @throws Error if login fails (401 Unauthorized)
   */
  const login = async (email, password) => {
    try {
      const response = await authService.login({ email, password });
      const { accessToken } = response.data;
      
      // ✅ Store JWT token
      localStorage.setItem('token', accessToken);
      
      // ✅ Decode token to get user info
      const userData = decodeToken(accessToken);
      setUser(userData);
      
      return userData;
    } catch (error) {
      // Error already handled by Axios interceptor
      throw error;
    }
  };

  /**
   * Register new user → Auto-login after registration
   * 
   * Flow:
   * 1. Call POST /api/auth/register with { email, password, phone }
   * 2. Backend returns: { id, email, role, status, ... } (NO password)
   * 3. Auto-login to get JWT token
   * 4. Store token and set user state
   * 
   * @param userData { email, password, phone, role }
   * @returns User object { id, email, role, exp }
   * @throws Error if registration fails (409 Conflict if email exists)
   */
  const register = async (userData) => {
    try {
      // Step 1: Register user
      await authService.register(userData);
      
      // Step 2: Auto-login to get JWT token
      const loginResponse = await authService.login({
        email: userData.email,
        password: userData.password
      });
      
      const { accessToken } = loginResponse.data;
      
      // ✅ Store JWT token
      localStorage.setItem('token', accessToken);
      
      // ✅ Decode token to get user info
      const userDataResponse = decodeToken(accessToken);
      setUser(userDataResponse);
      
      return userDataResponse;
    } catch (error) {
      throw error;
    }
  };

  /**
   * Logout - Clear authentication data
   * 
   * Removes:
   * - JWT token from localStorage
   * - User state
   */
  const logout = () => {
    localStorage.removeItem('token');
    setUser(null);
  };

  const value = {
    user,
    login,
    register,
    logout,
    loading,
    isAuthenticated: !!user,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
};

