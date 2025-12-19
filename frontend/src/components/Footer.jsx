import { Link } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import '../styles/components/Footer.css';

const Footer = () => {
  const { isAuthenticated } = useAuth();

  return (
    <footer className="footer">
      <div className="footer-container">
        {/* Main Footer Content */}
        <div className="footer-main">
          {/* Brand Section */}
          <div className="footer-brand">
            <Link to="/" className="footer-logo-link">
              <div className="footer-logo">
                <div className="footer-logo-icon">✈</div>
                <span className="footer-logo-text">Flight Network</span>
              </div>
            </Link>
            <p className="footer-tagline">
            Tìm vé máy bay giá rẻ nhất từ ​​tất cả các hãng hàng không.
            </p>
          </div>

          {/* Contact Information */}
          <div className="footer-section footer-contact-section">
            <h4 className="footer-title">Liên Hệ Chúng Tôi</h4>
            <ul className="footer-contact">
              <li>
                
                <a href="mailto:1250080225@sv.hcmunre.edu.vn">1250080225@sv.hcmunre.edu.vn</a>
              </li>
              <li>
               
                <a href="tel:+84352335245">+84 0352335245 (Mr. Trần Phạm Thanh Tùng)</a>
              </li>
              <li>
             
                <span>Hỗ trợ 24/7</span>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
