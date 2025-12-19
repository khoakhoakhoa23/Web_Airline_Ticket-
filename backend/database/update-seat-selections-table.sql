-- Update seat_selections table to add booking_id and status columns
-- This aligns with the standard booking flow design

-- Add booking_id column if not exists
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'seat_selections' AND column_name = 'booking_id'
    ) THEN
        ALTER TABLE seat_selections ADD COLUMN booking_id VARCHAR(255);
        
        -- Update existing records: try to find booking_id from passengers
        UPDATE seat_selections ss
        SET booking_id = (
            SELECT p.booking_id 
            FROM passengers p 
            WHERE p.id = ss.passenger_id 
            LIMIT 1
        )
        WHERE ss.booking_id IS NULL;
        
        -- Make booking_id NOT NULL after updating existing records
        ALTER TABLE seat_selections ALTER COLUMN booking_id SET NOT NULL;
        
        -- Add foreign key
        ALTER TABLE seat_selections 
        ADD CONSTRAINT fk_seat_selections_booking 
        FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE;
    END IF;
END $$;

-- Add status column if not exists
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'seat_selections' AND column_name = 'status'
    ) THEN
        ALTER TABLE seat_selections ADD COLUMN status VARCHAR(20) DEFAULT 'RESERVED';
        
        -- Update existing records: set status based on booking status
        UPDATE seat_selections ss
        SET status = CASE 
            WHEN EXISTS (
                SELECT 1 FROM bookings b 
                WHERE b.id = ss.booking_id 
                AND b.status = 'CONFIRMED'
            ) THEN 'CONFIRMED'
            ELSE 'RESERVED'
        END;
        
        -- Make status NOT NULL
        ALTER TABLE seat_selections ALTER COLUMN status SET NOT NULL;
    END IF;
END $$;

-- Add indexes for performance
CREATE INDEX IF NOT EXISTS idx_seat_selections_booking_id ON seat_selections(booking_id);
CREATE INDEX IF NOT EXISTS idx_seat_selections_segment_seat ON seat_selections(segment_id, seat_number);
CREATE INDEX IF NOT EXISTS idx_seat_selections_status ON seat_selections(status);

-- Update comments
COMMENT ON COLUMN seat_selections.booking_id IS 'Reference to booking that includes this seat selection';
COMMENT ON COLUMN seat_selections.status IS 'RESERVED: Seat selected but not paid, CONFIRMED: Seat confirmed after payment, CANCELLED: Seat cancelled';

