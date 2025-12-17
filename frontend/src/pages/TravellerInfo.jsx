import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import '../styles/pages/TravellerInfo.css';

const TravellerInfo = () => {
  const { user } = useAuth();
  const [formData, setFormData] = useState({
    email: user?.email || '',
    mobileNumber: user?.phone || '',
    receiveNewsletters: false,
    passengers: [{
      title: 'MR',
      firstName: '',
      surname: '',
      dateOfBirth: '',
      gender: 'MALE',
      documentType: 'PASSPORT',
      documentNumber: '',
    }],
    lostBaggageService: false,
  });
  const [selectedFlight, setSelectedFlight] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    const stored = localStorage.getItem('selectedFlight');
    if (stored) {
      setSelectedFlight(JSON.parse(stored));
    } else {
      navigate('/');
    }
  }, [navigate]);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    if (name.startsWith('passenger.')) {
      const [_, index, field] = name.split('.');
      const updatedPassengers = [...formData.passengers];
      updatedPassengers[index] = {
        ...updatedPassengers[index],
        [field]: value,
      };
      setFormData({ ...formData, passengers: updatedPassengers });
    } else {
      setFormData({
        ...formData,
        [name]: type === 'checkbox' ? checked : value,
      });
    }
  };

  const handleContinue = () => {
    localStorage.setItem('travellerInfo', JSON.stringify(formData));
    navigate('/booking/extra-services');
  };

  if (!selectedFlight) {
    return <div>Loading...</div>;
  }

  const totalPrice = ((selectedFlight.baseFare || 0) + (selectedFlight.taxes || 0));

  return (
    <div className="traveller-info-page">
      <div className="progress-bar">
        <div className="progress-step completed">
          <div className="step-number">1</div>
          <span>Flight Section</span>
        </div>
        <div className="progress-line"></div>
        <div className="progress-step active">
          <div className="step-number">2</div>
          <span>Traveller information</span>
        </div>
        <div className="progress-line"></div>
        <div className="progress-step">
          <div className="step-number">3</div>
          <span>Payment</span>
        </div>
      </div>

      <div className="traveller-info-container">
        <div className="main-content">
          <a href="/booking/flight-selection" className="back-link">← Edit Traveler details</a>

          <div className="section">
            <h3>Contact information for all passengers</h3>
            <div className="form-row">
              <div className="form-group">
                <label>Email *</label>
                <input
                  type="email"
                  name="email"
                  value={formData.email}
                  onChange={handleChange}
                  placeholder="Email@yahoo.com"
                  required
                />
              </div>
              <div className="form-group">
                <label>Mobile Number *</label>
                <div className="phone-input">
                  <input type="text" value="+61" readOnly className="country-code" />
                  <input
                    type="tel"
                    name="mobileNumber"
                    value={formData.mobileNumber}
                    onChange={handleChange}
                    placeholder="12345679"
                    required
                  />
                </div>
              </div>
            </div>
            <label className="checkbox-label">
              <input
                type="checkbox"
                name="receiveNewsletters"
                checked={formData.receiveNewsletters}
                onChange={handleChange}
              />
              I do not wish to receive any newsletters about cheap air fares or other offers
            </label>
          </div>

          {formData.passengers.map((passenger, index) => (
            <div key={index} className="section">
              <h3>Passenger {index + 1}, adult</h3>
              <div className="form-row">
                <div className="form-group">
                  <label>Title</label>
                  <div className="radio-group">
                    <label>
                      <input
                        type="radio"
                        name={`passenger.${index}.title`}
                        value="MR"
                        checked={passenger.title === 'MR'}
                        onChange={handleChange}
                      />
                      Mr
                    </label>
                    <label>
                      <input
                        type="radio"
                        name={`passenger.${index}.title`}
                        value="MRS"
                        checked={passenger.title === 'MRS'}
                        onChange={handleChange}
                      />
                      Mrs/Ms
                    </label>
                  </div>
                </div>
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>First name *</label>
                  <input
                    type="text"
                    name={`passenger.${index}.firstName`}
                    value={passenger.firstName}
                    onChange={handleChange}
                    required
                  />
                </div>
                <div className="form-group">
                  <label>Surname *</label>
                  <input
                    type="text"
                    name={`passenger.${index}.surname`}
                    value={passenger.surname}
                    onChange={handleChange}
                    required
                  />
                </div>
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>Date of Birth *</label>
                  <input
                    type="date"
                    name={`passenger.${index}.dateOfBirth`}
                    value={passenger.dateOfBirth}
                    onChange={handleChange}
                    required
                  />
                </div>
                <div className="form-group">
                  <label>Gender *</label>
                  <select
                    name={`passenger.${index}.gender`}
                    value={passenger.gender}
                    onChange={handleChange}
                    required
                  >
                    <option value="MALE">Male</option>
                    <option value="FEMALE">Female</option>
                  </select>
                </div>
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>Document Type *</label>
                  <select
                    name={`passenger.${index}.documentType`}
                    value={passenger.documentType}
                    onChange={handleChange}
                    required
                  >
                    <option value="PASSPORT">Passport</option>
                    <option value="ID_CARD">ID Card</option>
                  </select>
                </div>
                <div className="form-group">
                  <label>Document Number *</label>
                  <input
                    type="text"
                    name={`passenger.${index}.documentNumber`}
                    value={passenger.documentNumber}
                    onChange={handleChange}
                    required
                  />
                </div>
              </div>
            </div>
          ))}

          <div className="section">
            <h3>Hand Baggage</h3>
            <div className="baggage-info">
              <span className="baggage-icon">👜</span>
              <div>
                <p>Hand baggage</p>
                <p>1X7 Kg</p>
                <span className="included">Included</span>
              </div>
            </div>
          </div>

          <div className="section">
            <h3>Checked baggage</h3>
            <div className="baggage-info">
              <span className="baggage-icon">🧳</span>
              <div>
                <p>Checked baggage</p>
                <p>1X23 Kg</p>
                <span className="included">Included</span>
              </div>
            </div>
          </div>

          <div className="section">
            <h3>Lost Baggage Service</h3>
            <div className="service-card">
              <div className="service-image">📷</div>
              <div className="service-content">
                <p>
                  Our partner Blue Ribbon Bags helps you track lost baggage for up to 96 hours. 
                  If your bag is not returned by then you will receive 500 USD.
                </p>
                <p className="service-price">Only Au$ 10.86</p>
                <div className="radio-group">
                  <label>
                    <input
                      type="radio"
                      name="lostBaggageService"
                      value="false"
                      checked={!formData.lostBaggageService}
                      onChange={(e) => setFormData({...formData, lostBaggageService: false})}
                    />
                    No
                  </label>
                  <label>
                    <input
                      type="radio"
                      name="lostBaggageService"
                      value="true"
                      checked={formData.lostBaggageService}
                      onChange={(e) => setFormData({...formData, lostBaggageService: true})}
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

export default TravellerInfo;

