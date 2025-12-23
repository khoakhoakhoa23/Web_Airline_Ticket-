import { Navigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

/**
 * Protected Route Component
 * 
 * Protects routes based on authentication and role
 * 
 * FIX: Prevent redirect during token restoration on page refresh
 * - Wait for AuthContext to finish loading before checking authentication
 * - This prevents race condition where ProtectedRoute redirects before token is restored
 * 
 * @param {React.ReactNode} children - Child components to render
 * @param {string[]} allowedRoles - Array of allowed roles (e.g., ['ADMIN', 'USER'])
 */
const ProtectedRoute = ({ children, allowedRoles = [] }) => {
  const { isAuthenticated, user, loading } = useAuth();

  // FIX: Wait for AuthContext to finish loading token from localStorage
  // When user refreshes page (F5), AuthContext needs time to:
  // 1. Read token from localStorage
  // 2. Check if token is expired
  // 3. Decode token to get user info
  // 4. Set user state
  // 
  // If we check isAuthenticated before loading completes, user will be null
  // and we'll incorrectly redirect to login even though token exists
  if (loading) {
    // Show loading state while checking authentication
    // This prevents premature redirect during token restoration
    return (
      <div style={{ 
        display: 'flex', 
        justifyContent: 'center', 
        alignItems: 'center', 
        minHeight: '50vh' 
      }}>
        <div>Loading...</div>
      </div>
    );
  }

  // Check if user is authenticated (after loading completes)
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  // Check if user has required role
  if (allowedRoles.length > 0) {
    const hasRequiredRole = allowedRoles.includes(user?.role);
    
    if (!hasRequiredRole) {
      // Redirect non-admin users trying to access admin routes
      return <Navigate to="/" replace />;
    }
  }

  return children;
};

export default ProtectedRoute;

