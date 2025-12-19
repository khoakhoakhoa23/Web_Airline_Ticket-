import '../styles/pages/Contact.css';

const Contact = () => {
  return (
    <div className="contact-page">
      <div className="contact-container">
        <h1 className="contact-title">Contact Us</h1>
        <p className="contact-subtitle">We're here to help! Get in touch with us.</p>

        <div className="contact-content">
          <div className="contact-info">
            <div className="contact-card">
              <div className="contact-icon">📧</div>
              <h3>Email</h3>
              <p>Send us an email anytime!</p>
              <a href="mailto:support@flightnetwork.com" className="contact-link">
                support@flightnetwork.com
              </a>
            </div>

            <div className="contact-card">
              <div className="contact-icon">📞</div>
              <h3>Phone</h3>
              <p>Call us 24/7</p>
              <a href="tel:+841800123456" className="contact-link">
                +84 1800 123 456
              </a>
            </div>

            <div className="contact-card">
              <div className="contact-icon">🕒</div>
              <h3>Support Hours</h3>
              <p>We're available around the clock</p>
              <span className="contact-text">24/7 Customer Support</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Contact;

