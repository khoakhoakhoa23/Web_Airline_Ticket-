import { Navigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

/**
 * Protected Route Component
 * 
 * Protects routes based on authentication and role
 * 
 * @param {React.ReactNode} children - Child components to render
 * @param {string[]} allowedRoles - Array of allowed roles (e.g., ['ADMIN', 'USER'])
 */
const ProtectedRoute = ({ children, allowedRoles = [] }) => {
  const { isAuthenticated, user } = useAuth();

  // Check if user is authenticated
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

