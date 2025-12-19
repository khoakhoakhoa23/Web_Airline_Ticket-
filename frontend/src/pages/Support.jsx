import '../styles/pages/Support.css';

const Support = () => {
  return (
    <div className="support-page">
      <div className="support-container">
        <h1 className="support-title">Support Center</h1>
        <p className="support-subtitle">Find answers to common questions and get help.</p>

        <div className="support-content">
          <div className="support-section">
            <h2>Frequently Asked Questions</h2>
            <div className="faq-list">
              <div className="faq-item">
                <h3>How do I change or cancel my booking?</h3>
                <p>You can manage your booking through the "My Bookings" page. Log in to view and modify your reservations.</p>
              </div>
              <div className="faq-item">
                <h3>What payment methods do you accept?</h3>
                <p>We accept all major credit cards (VISA, Mastercard, AMEX) through our secure Stripe payment gateway.</p>
              </div>
              <div className="faq-item">
                <h3>How do I get my e-ticket?</h3>
                <p>After successful payment, your e-ticket will be available in "My Bookings" and sent to your email.</p>
              </div>
            </div>
          </div>

          <div className="support-section">
            <h2>Need More Help?</h2>
            <p>If you can't find what you're looking for, please contact our support team:</p>
            <div className="support-contact">
              <a href="mailto:support@flightnetwork.com" className="support-link">
                📧 support@flightnetwork.com
              </a>
              <a href="tel:+841800123456" className="support-link">
                📞 +84 1800 123 456
              </a>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Support;

