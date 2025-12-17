import { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { flightService } from '../services/api';
import '../styles/pages/FlightSelection.css';

const FlightSelection = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  
  // State
  const [flights, setFlights] = useState([]);
  const [selectedFlight, setSelectedFlight] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [searchData, setSearchData] = useState(null);
  const [pagination, setPagination] = useState({
    page: 0,
    size: 10,
    totalElements: 0,
    totalPages: 0,
  });

  /**
   * Fetch flights from API
   */
  const fetchFlights = async (page = 0) => {
    setLoading(true);
    setError('');

    try {
      // Get query params
      const origin = searchParams.get('origin');
      const destination = searchParams.get('destination');
      const departureDate = searchParams.get('departureDate');
      const passengers = searchParams.get('passengers') || 1;
      const cabinClass = searchParams.get('cabinClass');

      // Validate required params
      if (!origin || !destination || !departureDate) {
        setError('Missing required search parameters. Please search again.');
        setLoading(false);
        return;
      }

      // Store search data for display
      setSearchData({
        origin,
        destination,
        departureDate,
        passengers,
        cabinClass,
      });

      // Call API
      const response = await flightService.searchFlights({
        origin,
        destination,
        departureDate,
        passengers: parseInt(passengers),
        cabinClass,
        page,
        size: 10,
      });

      // Handle response
      const data = response.data;
      setFlights(data.content || []);
      setPagination({
        page: data.number || 0,
        size: data.size || 10,
        totalElements: data.totalElements || 0,
        totalPages: data.totalPages || 0,
      });
    } catch (err) {
      console.error('Error fetching flights:', err);
      
      // Handle different error types
      if (err.response?.status === 400) {
        setError('Invalid search parameters. Please check your input and try again.');
      } else if (err.response?.status === 401) {
        setError('Your session has expired. Please login again.');
        // Note: Flight search is public, so 401 should not happen
      } else if (err.response?.status === 500) {
        setError('Server error. Please try again later.');
      } else {
        setError(err.message || 'Failed to search flights. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  };

  // Fetch flights on mount and when search params change
  useEffect(() => {
    // Check if search params exist
    const origin = searchParams.get('origin');
    const destination = searchParams.get('destination');
    const departureDate = searchParams.get('departureDate');

    if (!origin || !destination || !departureDate) {
      // Redirect to home if no search params
      navigate('/');
      return;
    }

    fetchFlights();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [searchParams, navigate]);

  /**
   * Handle flight selection
   */
  const handleSelectFlight = (flight) => {
    setSelectedFlight(flight);
    localStorage.setItem('selectedFlight', JSON.stringify(flight));
    navigate('/booking/traveller-info');
  };

  /**
   * Handle pagination - Load more flights
   */
  const handleLoadMore = () => {
    if (pagination.page + 1 < pagination.totalPages) {
      fetchFlights(pagination.page + 1);
    }
  };

  /**
   * Format date for display
   */
  const formatDate = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', { 
      weekday: 'short', 
      year: 'numeric', 
      month: 'short', 
      day: 'numeric' 
    });
  };

  /**
   * Format time for display
   */
  const formatTime = (dateString) => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleTimeString('en-US', { 
      hour: '2-digit', 
      minute: '2-digit',
      hour12: false
    });
  };

  /**
   * Format price in VND
   */
  const formatPrice = (price) => {
    if (!price) return '0';
    return new Intl.NumberFormat('vi-VN').format(price);
  };

  /**
   * Calculate flight duration in hours and minutes
   */
  const calculateDuration = (departTime, arriveTime) => {
    const depart = new Date(departTime);
    const arrive = new Date(arriveTime);
    const durationMinutes = Math.round((arrive - depart) / 60000);
    const hours = Math.floor(durationMinutes / 60);
    const minutes = durationMinutes % 60;
    return `${hours}h ${minutes}m`;
  };

  // Loading state
  if (loading && flights.length === 0) {
    return (
      <div className="flight-selection-page">
        <div className="loading-container">
          <div className="loading-spinner"></div>
          <p>Searching for flights...</p>
        </div>
      </div>
    );
  }

  // Error state with no search data
  if (error && !searchData) {
    return (
      <div className="flight-selection-page">
        <div className="error-container">
          <div className="error-icon">⚠️</div>
          <h2>Oops! Something went wrong</h2>
          <p className="error-message">{error}</p>
          <button onClick={() => navigate('/')} className="back-button">
            Back to Home
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="flight-selection-page">
      {/* Progress Bar */}
      <div className="progress-bar">
        <div className="progress-step completed">
          <div className="step-number">1</div>
          <span>Flight Selection</span>
        </div>
        <div className="progress-line"></div>
        <div className="progress-step">
          <div className="step-number">2</div>
          <span>Traveller Information</span>
        </div>
        <div className="progress-line"></div>
        <div className="progress-step">
          <div className="step-number">3</div>
          <span>Payment</span>
        </div>
      </div>

      <div className="flight-selection-container">
        <h2>Select Your Flight</h2>
        
        {/* Search Summary */}
        {searchData && (
          <div className="search-summary">
            <p>
              <strong>Route:</strong> {searchData.origin} → {searchData.destination} &nbsp;|&nbsp;
              <strong>Date:</strong> {formatDate(searchData.departureDate)} &nbsp;|&nbsp;
              <strong>Passengers:</strong> {searchData.passengers} &nbsp;|&nbsp;
              <strong>Class:</strong> {searchData.cabinClass || 'ECONOMY'}
            </p>
            <p className="results-count">
              {pagination.totalElements > 0 
                ? `Found ${pagination.totalElements} flight${pagination.totalElements !== 1 ? 's' : ''}`
                : 'No flights found'
              }
            </p>
          </div>
        )}

        {/* Error Message */}
        {error && (
          <div className="error-banner">
            <span className="error-icon">⚠️</span>
            <span className="error-text">{error}</span>
          </div>
        )}

        {/* Flights List */}
        <div className="flights-list">
          {flights.length === 0 && !loading ? (
            <div className="no-flights">
              <div className="no-flights-icon">✈️</div>
              <h3>No flights found</h3>
              <p>We couldn't find any flights matching your search criteria.</p>
              <p>Try adjusting your search parameters.</p>
              <button onClick={() => navigate('/')} className="back-button">
                Back to Search
              </button>
            </div>
          ) : (
            <>
              {flights.map((flight) => (
                <div
                  key={flight.id}
                  className={`flight-card ${selectedFlight?.id === flight.id ? 'selected' : ''}`}
                  onClick={() => handleSelectFlight(flight)}
                >
                  <div className="flight-info">
                    {/* Airline Info */}
                    <div className="flight-airline">
                      <strong className="airline-name">{flight.airline}</strong>
                      <span className="flight-number">{flight.flightNumber}</span>
                      <span className="aircraft-type">{flight.aircraftType}</span>
                    </div>

                    {/* Flight Route */}
                    <div className="flight-route">
                      <div className="route-time">
                        <span className="time">{formatTime(flight.departTime)}</span>
                        <span className="airport">{flight.origin}</span>
                      </div>
                      <div className="route-line">
                        <div className="line"></div>
                        <span className="duration">
                          {calculateDuration(flight.departTime, flight.arriveTime)}
                        </span>
                      </div>
                      <div className="route-time">
                        <span className="time">{formatTime(flight.arriveTime)}</span>
                        <span className="airport">{flight.destination}</span>
                      </div>
                    </div>

                    {/* Flight Details */}
                    <div className="flight-details">
                      <span className="cabin-class">
                        <strong>Class:</strong> {flight.cabinClass}
                      </span>
                      <span className="available-seats">
                        <strong>Seats:</strong> {flight.availableSeats}/{flight.totalSeats}
                      </span>
                      <span className="status">
                        <strong>Status:</strong> {flight.status}
                      </span>
                    </div>
                  </div>

                  {/* Price Section */}
                  <div className="flight-price">
                    <div className="price-breakdown">
                      <span className="price-label">Total Price</span>
                      <span className="price-amount">
                        {formatPrice(flight.totalPrice)} VND
                      </span>
                      <span className="price-details">
                        (Base: {formatPrice(flight.baseFare)} + Tax: {formatPrice(flight.taxes)})
                      </span>
                    </div>
                    <button className="select-button">
                      Select Flight →
                    </button>
                  </div>
                </div>
              ))}

              {/* Load More Button */}
              {pagination.page + 1 < pagination.totalPages && (
                <div className="load-more-container">
                  <button 
                    onClick={handleLoadMore} 
                    className="load-more-button"
                    disabled={loading}
                  >
                    {loading ? 'Loading...' : `Load More (${pagination.totalElements - flights.length} remaining)`}
                  </button>
                </div>
              )}

              {/* Pagination Info */}
              <div className="pagination-info">
                Showing {flights.length} of {pagination.totalElements} flights
                {pagination.totalPages > 1 && ` (Page ${pagination.page + 1} of ${pagination.totalPages})`}
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
};

export default FlightSelection;

