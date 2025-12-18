import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useBooking } from '../contexts/BookingContext';
import '../styles/pages/ExtraServices.css';

const ExtraServices = () => {
  const navigate = useNavigate();
  const { selectedFlight, passengers, updateExtraServices } = useBooking();
  
  const [selectedSupport, setSelectedSupport] = useState('STANDARD');
  const [medicalCover, setMedicalCover] = useState(false);
  const [collapseCover, setCollapseCover] = useState(false);
  const [travellerInfo, setTravellerInfo] = useState(null);

  useEffect(() => {
    // Check if we have required data
    if (!selectedFlight) {
      // Try to restore from localStorage
      const storedFlight = localStorage.getItem('selectedFlight');
      if (!storedFlight) {
        navigate('/airline-info');
        return;
      }
    }

    const traveller = localStorage.getItem('travellerInfo');
    if (traveller) {
      setTravellerInfo(JSON.parse(traveller));
    }
  }, [selectedFlight, navigate]);

  const handleContinue = () => {
    const extraServicesData = {
      supportPackage: selectedSupport,
      medicalCover,
      collapseCover,
    };
    
    // Save to BookingContext
    updateExtraServices(extraServicesData);
    
    navigate('/booking/payment');
  };

  if (!selectedFlight || !travellerInfo) {
    return <div>Loading...</div>;
  }

  const basePrice = ((selectedFlight.baseFare || 0) + (selectedFlight.taxes || 0));
  const supportPrice = selectedSupport === 'STANDARD' ? 56.93 : 58.49;
  const medicalPrice = medicalCover ? 70.86 : 0;
  const collapsePrice = collapseCover ? 18.86 : 0;
  const totalPrice = basePrice + supportPrice + medicalPrice + collapsePrice;

  return (
    <div className="extra-services-page">
      <div className="progress-bar">
        <div className="progress-step completed">
          <div className="step-number">1</div>
          <span>Flight Section</span>
        </div>
        <div className="progress-line"></div>
        <div className="progress-step completed">
          <div className="step-number">2</div>
          <span>Traveller information</span>
        </div>
        <div className="progress-line"></div>
        <div className="progress-step active">
          <div className="step-number">3</div>
          <span>Payment</span>
        </div>
      </div>

      <div className="extra-services-container">
        <div className="main-content">
          <a href="/booking/traveller-info" className="back-link">← Edit Traveler details</a>

          <div className="section">
            <div className="section-header">
              <div className="section-icon">🎧</div>
              <div>
                <h3>Choose your level of support</h3>
                <p>Every passenger has different support needs - choose the package that suits you best</p>
              </div>
            </div>
            <div className="support-packages">
              <div
                className={`support-package ${selectedSupport === 'STANDARD' ? 'selected' : ''}`}
                onClick={() => setSelectedSupport('STANDARD')}
              >
                <div className="package-badge">Most Popular!</div>
                <h4>Standard</h4>
                <div className="package-price">Au${supportPrice.toFixed(2)}</div>
                <p className="package-description">A good level of support at a fair cost.</p>
                <ul className="package-features">
                  <li>✓ Service fee applies</li>
                  <li>✓ Quick response time</li>
                  <li>✓ Flight updates by SMS</li>
                  <li>✓ Check-in</li>
                </ul>
                <button className="package-button">Select</button>
              </div>
              <div
                className={`support-package ${selectedSupport === 'PLATINUM' ? 'selected' : ''}`}
                onClick={() => setSelectedSupport('PLATINUM')}
              >
                <h4>Platinum</h4>
                <div className="package-price">Au$58.49</div>
                <p className="package-description">Our most comprehensive support package.</p>
                <ul className="package-features">
                  <li>✓ Service fee waiver</li>
                  <li>✓ Quick response time</li>
                  <li>✓ Flight updates by SMS</li>
                  <li>✓ Check-in</li>
                </ul>
                <button className="package-button">Select</button>
              </div>
            </div>
          </div>

          <div className="section">
            <div className="section-header">
              <div className="section-icon">📱</div>
              <div>
                <h3>Choose the best coverage if things go wrong on your trip</h3>
                <p>Select your insurance from our generous cover options.</p>
              </div>
            </div>
            <div className="insurance-options">
              <div className="insurance-option">
                <div className="insurance-content">
                  <h4>Emergency Medical & Expenses Cover</h4>
                  <p>Coverage for medical emergencies and injuries. Only available to people 72 years old and younger.</p>
                  <div className="insurance-price">Total per person: AU$ 70.86</div>
                </div>
                <div className="radio-group">
                  <label>
                    <input
                      type="radio"
                      name="medicalCover"
                      value="false"
                      checked={!medicalCover}
                      onChange={() => setMedicalCover(false)}
                    />
                    No
                  </label>
                  <label>
                    <input
                      type="radio"
                      name="medicalCover"
                      value="true"
                      checked={medicalCover}
                      onChange={() => setMedicalCover(true)}
                    />
                    Yes
                  </label>
                </div>
              </div>
              <div className="insurance-option">
                <div className="insurance-content">
                  <h4>Airline Collapse Cover</h4>
                  <p>
                    Protection if the airline declares bankruptcy or cancels flights due to COVID-19 or other reasons, 
                    offering refunds for prepaid airfares or covering return airfares if the trip has begun.
                  </p>
                  <div className="insurance-price">Total per person: AU$ 18.86</div>
                </div>
                <div className="radio-group">
                  <label>
                    <input
                      type="radio"
                      name="collapseCover"
                      value="false"
                      checked={!collapseCover}
                      onChange={() => setCollapseCover(false)}
                    />
                    No
                  </label>
                  <label>
                    <input
                      type="radio"
                      name="collapseCover"
                      value="true"
                      checked={collapseCover}
                      onChange={() => setCollapseCover(true)}
                    />
                    Yes
                  </label>
                </div>
              </div>
            </div>
          </div>

          <p className="confirmation-text">
            By booking you confirm that the names on the booking match those on the passports of those travelling.
          </p>

          <button onClick={handleContinue} className="continue-button">
            Continue
          </button>
        </div>

        <div className="order-summary">
          <h3>Your Order</h3>
          <div className="order-section">
            <div className="order-item">
              <span className="order-icon">✈</span>
              <div>
                <p><strong>Departure</strong></p>
                <p>{new Date(selectedFlight.departTime).toLocaleDateString('en-US', { weekday: 'short', year: 'numeric', month: 'short', day: 'numeric' })}</p>
                <p>{new Date(selectedFlight.departTime).toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })} - {new Date(selectedFlight.arriveTime).toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })}</p>
                <p>{selectedFlight.origin} - {selectedFlight.destination}</p>
              </div>
            </div>
          </div>
          <div className="order-section">
            <p><strong>Bags</strong></p>
            <p>👜 Hand baggage: 1X7 Kg</p>
            <p>🧳 Checked baggage: 1X23 Kg</p>
          </div>
          <div className="order-total">
            <p>Total: <strong>AU${totalPrice.toLocaleString()}</strong></p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ExtraServices;

