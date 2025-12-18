import { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { seatSelectionService } from '../services/api';
import { useBooking } from '../contexts/BookingContext';
import '../styles/pages/SeatSelection.css';

const SeatSelection = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { selectFlight, selectSeats: saveSeatsToContext } = useBooking();
  const flight = location.state?.preselectedFlight || location.state?.selectedFlight;
  
  const [selectedSeats, setSelectedSeats] = useState([]);
  const [bookedSeats, setBookedSeats] = useState([]);
  const [loading, setLoading] = useState(true);
  const [numPassengers, setNumPassengers] = useState(location.state?.passengers || 1);

  useEffect(() => {
    if (!flight) {
      navigate('/airline-info');
      return;
    }
    loadBookedSeats();
  }, [flight]);

  const loadBookedSeats = async () => {
    try {
      setLoading(true);
      
      // Fetch booked seats from API
      if (flight && flight.flightNumber) {
        const response = await seatSelectionService.getBookedSeatsByFlightNumber(flight.flightNumber);
        
        const bookedSeatsData = response.data || [];
        // Extract just the seat numbers
        const booked = bookedSeatsData
          .filter(seat => seat.seatNumber)
          .map(seat => seat.seatNumber);
        
        setBookedSeats(booked);
      } else {
        setBookedSeats([]);
      }
    } catch (err) {
      console.error('Failed to load booked seats:', err);
      // If API fails, fall back to empty array
      setBookedSeats([]);
    } finally {
      setLoading(false);
    }
  };

  const generateSeats = () => {
    const totalSeats = flight.totalSeats || 180;
    const rows = Math.ceil(totalSeats / 6); // 6 seats per row (A-F)
    const seats = [];

    for (let row = 1; row <= rows; row++) {
      const seatRow = [];
      for (let col = 0; col < 6; col++) {
        const seatLabel = `${row}${String.fromCharCode(65 + col)}`;
        const isBooked = bookedSeats.includes(seatLabel);
        const isSelected = selectedSeats.includes(seatLabel);
        
        seatRow.push({
          label: seatLabel,
          row: row,
          col: String.fromCharCode(65 + col),
          isBooked,
          isSelected
        });
      }
      seats.push(seatRow);
    }

    return seats;
  };

  const handleSeatClick = (seat) => {
    if (seat.isBooked) return;

    if (selectedSeats.includes(seat.label)) {
      setSelectedSeats(selectedSeats.filter(s => s !== seat.label));
    } else {
      if (selectedSeats.length < numPassengers) {
        setSelectedSeats([...selectedSeats, seat.label]);
      } else {
        alert(`Bạn chỉ có thể chọn ${numPassengers} ghế!`);
      }
    }
  };

  const getSeatClass = (seat) => {
    if (seat.isBooked) return 'seat booked';
    if (seat.isSelected) return 'seat selected';
    return 'seat available';
  };

  const getPriceForSeat = (seatLabel) => {
    const row = parseInt(seatLabel);
    // Front seats (rows 1-10) are premium
    if (row <= 10) return 500000;
    // Exit rows (usually rows 12-15) are premium
    if (row >= 12 && row <= 15) return 300000;
    // Regular seats (rows 11, 16+) have standard fee
    return 100000;
  };

  const getTotalSeatPrice = () => {
    return selectedSeats.reduce((sum, seat) => sum + getPriceForSeat(seat), 0);
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(amount);
  };

  const handleContinue = () => {
    if (selectedSeats.length === 0) {
      alert('Vui lòng chọn ít nhất 1 ghế!');
      return;
    }

    if (selectedSeats.length < numPassengers) {
      if (!confirm(`Bạn mới chọn ${selectedSeats.length}/${numPassengers} ghế. Tiếp tục?`)) {
        return;
      }
    }

    // Save to Booking Context
    selectFlight(flight);
    saveSeatsToContext(selectedSeats, getTotalSeatPrice());

    navigate('/booking/traveller-info', {
      state: {
        ...location.state,
        selectedFlight: flight,
        selectedSeats: selectedSeats,
        seatPrice: getTotalSeatPrice(),
        passengers: numPassengers
      }
    });
  };

  const handleSkip = () => {
    // Save to Booking Context
    selectFlight(flight);
    saveSeatsToContext([], 0);

    navigate('/booking/traveller-info', {
      state: {
        ...location.state,
        selectedFlight: flight,
        selectedSeats: [],
        seatPrice: 0,
        passengers: numPassengers
      }
    });
  };

  if (loading) {
    return (
      <div className="seat-selection-page">
        <div className="loading-container">
          <div className="spinner"></div>
          <p>Đang tải sơ đồ ghế...</p>
        </div>
      </div>
    );
  }

  if (!flight) {
    return null;
  }

  const seats = generateSeats();

  return (
    <div className="seat-selection-page">
      <div className="seat-selection-container">
        {/* Header */}
        <div className="page-header">
          <h1 className="page-title">Chọn Ghế Ngồi</h1>
          <p className="page-subtitle">
            Chuyến bay {flight.flightNumber} | {flight.origin} → {flight.destination}
          </p>
        </div>

        <div className="content-wrapper">
          {/* Seat Map */}
          <div className="seat-map-section">
            <div className="plane-header">
              <div className="cockpit">✈️ PHÍA TRƯỚC MÁY BAY</div>
            </div>

            {/* Legend */}
            <div className="seat-legend">
              <div className="legend-item">
                <div className="seat available"></div>
                <span>Còn trống</span>
              </div>
              <div className="legend-item">
                <div className="seat selected"></div>
                <span>Đã chọn</span>
              </div>
              <div className="legend-item">
                <div className="seat booked"></div>
                <span>Đã đặt</span>
              </div>
              <div className="legend-item">
                <div className="exit-row">EXIT</div>
                <span>Cửa thoát hiểm</span>
              </div>
            </div>

            {/* Seat Grid */}
            <div className="seat-grid">
              {/* Column Labels */}
              <div className="column-labels">
                <span className="row-label"></span>
                <span>A</span>
                <span>B</span>
                <span>C</span>
                <span className="aisle-space"></span>
                <span>D</span>
                <span>E</span>
                <span>F</span>
              </div>

              {/* Rows */}
              {seats.map((row, rowIndex) => (
                <div key={rowIndex} className="seat-row">
                  <span className="row-label">{rowIndex + 1}</span>
                  
                  {/* Seats A, B, C */}
                  {row.slice(0, 3).map((seat) => (
                    <button
                      key={seat.label}
                      className={getSeatClass(seat)}
                      onClick={() => handleSeatClick(seat)}
                      disabled={seat.isBooked}
                      title={seat.isBooked ? 'Ghế đã đặt' : `Ghế ${seat.label}`}
                    >
                      <span className="seat-label">{seat.col}</span>
                    </button>
                  ))}
                  
                  {/* Aisle space */}
                  <div className="aisle-space"></div>
                  
                  {/* Seats D, E, F */}
                  {row.slice(3, 6).map((seat) => (
                    <button
                      key={seat.label}
                      className={getSeatClass(seat)}
                      onClick={() => handleSeatClick(seat)}
                      disabled={seat.isBooked}
                      title={seat.isBooked ? 'Ghế đã đặt' : `Ghế ${seat.label}`}
                    >
                      <span className="seat-label">{seat.col}</span>
                    </button>
                  ))}
                  
                  <span className="row-label">{rowIndex + 1}</span>
                  
                  {/* Exit row indicator */}
                  {(rowIndex + 1 === 12 || rowIndex + 1 === 15) && (
                    <div className="exit-indicator">
                      <span className="exit-label">EXIT</span>
                    </div>
                  )}
                </div>
              ))}
            </div>

            <div className="plane-footer">
              <div className="tail">🔚 PHÍA SAU MÁY BAY</div>
            </div>
          </div>

          {/* Summary Panel */}
          <div className="summary-panel">
            <div className="summary-card">
              <h3 className="summary-title">Thông Tin Chuyến Bay</h3>
              
              <div className="flight-info">
                <div className="info-row">
                  <span className="info-label">Chuyến bay:</span>
                  <span className="info-value">{flight.flightNumber}</span>
                </div>
                <div className="info-row">
                  <span className="info-label">Hãng bay:</span>
                  <span className="info-value">{flight.airline}</span>
                </div>
                <div className="info-row">
                  <span className="info-label">Tuyến bay:</span>
                  <span className="info-value">{flight.origin} → {flight.destination}</span>
                </div>
                <div className="info-row">
                  <span className="info-label">Hạng ghế:</span>
                  <span className="info-value">{flight.cabinClass}</span>
                </div>
              </div>

              <div className="divider"></div>

              <h3 className="summary-title">Ghế Đã Chọn</h3>
              
              <div className="selected-seats-list">
                {selectedSeats.length > 0 ? (
                  selectedSeats.map((seat) => (
                    <div key={seat} className="selected-seat-item">
                      <div className="seat-info">
                        <span className="seat-number">Ghế {seat}</span>
                        <span className="seat-price">
                          {getPriceForSeat(seat) > 0 ? `+${formatCurrency(getPriceForSeat(seat))}` : 'Miễn phí'}
                        </span>
                      </div>
                      <button
                        className="remove-seat-btn"
                        onClick={() => setSelectedSeats(selectedSeats.filter(s => s !== seat))}
                      >
                        ×
                      </button>
                    </div>
                  ))
                ) : (
                  <p className="no-seats-selected">Chưa chọn ghế nào</p>
                )}
              </div>

              <div className="divider"></div>

              <div className="price-summary">
                <div className="price-row">
                  <span>Số hành khách:</span>
                  <span className="price-value">{numPassengers}</span>
                </div>
                <div className="price-row">
                  <span>Đã chọn:</span>
                  <span className="price-value">{selectedSeats.length} ghế</span>
                </div>
                <div className="price-row total">
                  <span>Phí chọn ghế:</span>
                  <span className="price-value">{formatCurrency(getTotalSeatPrice())}</span>
                </div>
              </div>

              <div className="action-buttons">
                <button className="skip-btn" onClick={handleSkip}>
                  Bỏ qua
                </button>
                <button 
                  className="continue-btn" 
                  onClick={handleContinue}
                  disabled={selectedSeats.length === 0}
                >
                  Tiếp tục
                </button>
              </div>

              <p className="note">
                💡 <strong>Lưu ý:</strong> Ghế ở hàng đầu và cửa thoát hiểm có phụ phí.
                Bạn có thể bỏ qua bước này để tự động phân ghế.
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SeatSelection;
