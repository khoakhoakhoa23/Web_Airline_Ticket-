import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import '../styles/pages/Home.css';

// Sử dụng hình ảnh từ public folder
// File: public/images/airplane-sky-home.jpg
const airplaneImage = '/images/airplane-sky-home.jpg';

// Cách 2: Từ assets folder (uncomment nếu muốn dùng)
// import airplaneImageSrc from '../assets/images/airplane-sky.jpg';
// const airplaneImage = airplaneImageSrc;

const Home = () => {
  const [searchForm, setSearchForm] = useState({
    tripType: 'ONE_WAY',
    from: '',
    to: '',
    departDate: '',
    returnDate: '',
    passengers: 1,
    class: 'ECONOMY',
  });
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleChange = (e) => {
    const { name, value } = e.target;
    setSearchForm({
      ...searchForm,
      [name]: value,
    });
  };

  const swapLocations = () => {
    setSearchForm({
      ...searchForm,
      from: searchForm.to,
      to: searchForm.from,
    });
  };

  /**
   * Handle flight search form submission
   * Validates inputs and redirects to FlightSelection page with query params
   */
  const handleSearch = (e) => {
    e.preventDefault();
    setError('');
    
    // Validation
    if (!searchForm.from || !searchForm.to || !searchForm.departDate) {
      setError('Please fill in all required fields');
      return;
    }

    // Build query params
    const params = new URLSearchParams({
      origin: searchForm.from.toUpperCase(),
      destination: searchForm.to.toUpperCase(),
      departureDate: searchForm.departDate,
      passengers: searchForm.passengers,
      cabinClass: searchForm.class,
    });

    // Navigate to flight selection with query params
    navigate(`/flights?${params.toString()}`);
  };

  return (
    <div className="home-page">
      <div className="hero-section">
        <div 
          className="hero-background"
          style={airplaneImage ? {
            backgroundImage: `url(${airplaneImage})`
          } : {}}
        ></div>
        <div className="hero-content">
          <h1 className="hero-title">Find the cheapest airline tickets across all airlines.</h1>
          <div className="search-form-container">
            <h2 className="search-form-title">Book Cheap Flight</h2>
            <form onSubmit={handleSearch} className="search-form">
              {error && <div className="error-message">{error}</div>}
              
              <div className="trip-type">
                <label>
                  <input
                    type="radio"
                    name="tripType"
                    value="RETURN"
                    checked={searchForm.tripType === 'RETURN'}
                    onChange={handleChange}
                  />
                  Return
                </label>
                <label>
                  <input
                    type="radio"
                    name="tripType"
                    value="ONE_WAY"
                    checked={searchForm.tripType === 'ONE_WAY'}
                    onChange={handleChange}
                  />
                  One - Way
                </label>
                <label>
                  <input
                    type="radio"
                    name="tripType"
                    value="MULTI_WAY"
                    checked={searchForm.tripType === 'MULTI_WAY'}
                    onChange={handleChange}
                  />
                  Multi - Way
                </label>
              </div>

              <div className="location-inputs">
                <div className="input-group">
                  <label>From</label>
                  <input
                    type="text"
                    name="from"
                    value={searchForm.from}
                    onChange={handleChange}
                    placeholder="Melbourne"
                    required
                  />
                </div>
                <button
                  type="button"
                  className="swap-button"
                  onClick={swapLocations}
                  aria-label="Swap locations"
                >
                  ⇄
                </button>
                <div className="input-group">
                  <label>To</label>
                  <input
                    type="text"
                    name="to"
                    value={searchForm.to}
                    onChange={handleChange}
                    placeholder="Perth"
                    required
                  />
                </div>
              </div>

              <div className="date-inputs">
                <div className="input-group">
                  <label>Depart</label>
                  <input
                    type="date"
                    name="departDate"
                    value={searchForm.departDate}
                    onChange={handleChange}
                    required
                  />
                </div>
                {searchForm.tripType === 'RETURN' && (
                  <div className="input-group">
                    <label>Return</label>
                    <input
                      type="date"
                      name="returnDate"
                      value={searchForm.returnDate}
                      onChange={handleChange}
                      min={searchForm.departDate}
                    />
                  </div>
                )}
              </div>

              <div className="passenger-class">
                <div className="input-group">
                  <label>Passengers</label>
                  <select
                    name="passengers"
                    value={searchForm.passengers}
                    onChange={handleChange}
                  >
                    {[1, 2, 3, 4, 5, 6, 7, 8, 9].map((num) => (
                      <option key={num} value={num}>
                        {num} {num === 1 ? 'adult' : 'adults'}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="input-group">
                  <label>Class</label>
                  <select
                    name="class"
                    value={searchForm.class}
                    onChange={handleChange}
                  >
                    <option value="ECONOMY">Economy</option>
                    <option value="BUSINESS">Business</option>
                    <option value="FIRST">First</option>
                  </select>
                </div>
              </div>

              <button type="submit" className="search-button">
                Search Flights
              </button>
            </form>
          </div>
        </div>
      </div>

      <div className="info-sections">
        <div className="info-card">
          <h3>Flexible Booking</h3>
          <p>
            Many airlines are relaxing their rules on ticket changes. They are saying it's OK if you are not totally sure; book now, and know that you can change your mind later.
          </p>
          <a href="#read-more" className="read-more">Read More</a>
        </div>
        <div className="info-card">
          <h3>Covid-19 Information</h3>
          <p>
            You can find some of the special requirements for any destination via the link below or directly on your airline's website. Please also consult your local health authorities for updated information.
          </p>
          <a href="#read-more" className="read-more">Read More</a>
        </div>
        <div className="info-card">
          <h3>Refunds</h3>
          <p>How does the refund process work?</p>
          <a href="#read-more" className="read-more">Read More</a>
        </div>
      </div>
    </div>
  );
};

export default Home;

