import '../styles/pages/Terms.css';

const Terms = () => {
  return (
    <div className="terms-page">
      <div className="terms-container">
        <h1 className="terms-title">Terms of Service</h1>
        <p className="terms-last-updated">Last updated: {new Date().toLocaleDateString()}</p>

        <div className="terms-content">
          <section className="terms-section">
            <h2>1. Acceptance of Terms</h2>
            <p>
              By accessing and using Flight Network, you accept and agree to be bound by the terms and provision of this agreement.
            </p>
          </section>

          <section className="terms-section">
            <h2>2. Booking Terms</h2>
            <p>
              All bookings are subject to availability and confirmation. Prices are subject to change until payment is completed.
              Once payment is confirmed, your booking is final and subject to our cancellation policy.
            </p>
          </section>

          <section className="terms-section">
            <h2>3. Payment</h2>
            <p>
              Payment must be completed within the specified time frame. We accept major credit cards through our secure payment gateway.
              All transactions are processed securely.
            </p>
          </section>

          <section className="terms-section">
            <h2>4. Cancellation and Refunds</h2>
            <p>
              Cancellation policies vary by airline and fare type. Please review your booking details for specific cancellation terms.
              Refunds, if applicable, will be processed according to the airline's policy.
            </p>
          </section>

          <section className="terms-section">
            <h2>5. Contact</h2>
            <p>
              For questions about these Terms of Service, please contact us at{' '}
              <a href="mailto:support@flightnetwork.com">support@flightnetwork.com</a>
            </p>
          </section>
        </div>
      </div>
    </div>
  );
};

export default Terms;

