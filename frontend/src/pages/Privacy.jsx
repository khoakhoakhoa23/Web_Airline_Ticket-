import '../styles/pages/Privacy.css';

const Privacy = () => {
  return (
    <div className="privacy-page">
      <div className="privacy-container">
        <h1 className="privacy-title">Privacy Policy</h1>
        <p className="privacy-last-updated">Last updated: {new Date().toLocaleDateString()}</p>

        <div className="privacy-content">
          <section className="privacy-section">
            <h2>1. Information We Collect</h2>
            <p>
              We collect information that you provide directly to us, including your name, email address, phone number,
              passport details, and payment information when you make a booking.
            </p>
          </section>

          <section className="privacy-section">
            <h2>2. How We Use Your Information</h2>
            <p>
              We use the information we collect to process your bookings, send booking confirmations, communicate with you
              about your travel, and improve our services.
            </p>
          </section>

          <section className="privacy-section">
            <h2>3. Data Security</h2>
            <p>
              We implement appropriate security measures to protect your personal information. All payment transactions
              are encrypted and processed through secure payment gateways.
            </p>
          </section>

          <section className="privacy-section">
            <h2>4. Sharing Your Information</h2>
            <p>
              We share your information with airlines and service providers necessary to complete your booking.
              We do not sell your personal information to third parties.
            </p>
          </section>

          <section className="privacy-section">
            <h2>5. Your Rights</h2>
            <p>
              You have the right to access, update, or delete your personal information. You can manage your account
              settings or contact us for assistance.
            </p>
          </section>

          <section className="privacy-section">
            <h2>6. Contact Us</h2>
            <p>
              If you have questions about this Privacy Policy, please contact us at{' '}
              <a href="mailto:support@flightnetwork.com">support@flightnetwork.com</a>
            </p>
          </section>
        </div>
      </div>
    </div>
  );
};

export default Privacy;

