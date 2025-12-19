-- Create seat_locks table for managing temporary seat locks
-- This prevents race conditions when multiple users select the same seat

CREATE TABLE IF NOT EXISTS seat_locks (
    id VARCHAR(255) PRIMARY KEY,
    flight_number VARCHAR(50) NOT NULL,
    segment_id VARCHAR(255),
    seat_number VARCHAR(10) NOT NULL,
    user_id VARCHAR(255),
    session_id VARCHAR(255), -- For anonymous users
    locked_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'LOCKED', -- LOCKED, RELEASED, CONFIRMED
    booking_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (segment_id) REFERENCES flight_segments(id) ON DELETE CASCADE,
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_seat_locks_flight_seat ON seat_locks(flight_number, seat_number);
CREATE INDEX IF NOT EXISTS idx_seat_locks_expires_at ON seat_locks(expires_at);
CREATE INDEX IF NOT EXISTS idx_seat_locks_status ON seat_locks(status);
CREATE INDEX IF NOT EXISTS idx_seat_locks_user_id ON seat_locks(user_id);
CREATE INDEX IF NOT EXISTS idx_seat_locks_session_id ON seat_locks(session_id);
CREATE INDEX IF NOT EXISTS idx_seat_locks_booking_id ON seat_locks(booking_id);

-- Unique constraint: Only one active lock per seat per flight
-- Note: This is enforced at application level, not database level
-- because we need to allow multiple locks with different statuses

COMMENT ON TABLE seat_locks IS 'Manages temporary seat locks during booking process (15 minutes duration)';
COMMENT ON COLUMN seat_locks.status IS 'LOCKED: Active lock, RELEASED: Lock expired/released, CONFIRMED: Lock confirmed after payment';

