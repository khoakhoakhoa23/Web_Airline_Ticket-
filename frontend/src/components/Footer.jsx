import { Link } from 'react-router-dom';
import '../styles/components/Footer.css';

const Footer = () => {
  return (
    <footer className="footer">
      <div className="footer-container">
        <div className="footer-columns">
          <div className="footer-column">
            <h4>Products and services</h4>
            <Link to="/cancellation-protection">Cancellation Protection</Link>
          </div>
          <div className="footer-column">
            <h4>About Us</h4>
            <Link to="/about">About Us</Link>
            <Link to="/terms">Terms and Conditions</Link>
          </div>
          <div className="footer-column">
            <h4>Support</h4>
            <Link to="/contact">Contact us</Link>
            <Link to="/faq">FAQ</Link>
          </div>
          <div className="footer-column">
            <h4>Login</h4>
            <Link to="/my-bookings" className="footer-login-link">
              <span className="login-icon">👤</span>
              My Booking
            </Link>
          </div>
        </div>
        <div className="footer-payment">
          <div className="payment-logos">
            <span className="payment-logo">VISA</span>
            <span className="payment-logo">Mastercard</span>
            <span className="payment-logo">AMEX</span>
          </div>
          <div className="partner-logos">
            <span className="partner-logo">IATA</span>
            <span className="partner-logo">AMADEUS</span>
            <span className="partner-logo">Sabre</span>
          </div>
        </div>
      </div>
    </footer>
  );
};

export default Footer;

