import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import '../styles/components/Header.css';

const Header = () => {
  const { user, logout, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <header className="header">
      <div className="header-container">
        <div className="header-brand">
          <Link to="/" className="logo-link">
            <div className="logo">
              <div className="logo-icon">✈</div>
              <span className="logo-text">Flight Network</span>
            </div>
          </Link>
        </div>
        <nav className="header-nav">
          <Link to="/airline-info" className="nav-link">Airline information</Link>
          {isAuthenticated ? (
            <>
              <Link to="/my-bookings" className="nav-link">My Booking</Link>
              <button onClick={handleLogout} className="nav-button">Logout</button>
            </>
          ) : (
            <Link to="/login" className="nav-link">Login</Link>
          )}
        </nav>
      </div>
    </header>
  );
};

export default Header;

