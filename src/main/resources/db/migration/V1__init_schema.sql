CREATE TABLE shipments (
                           id BIGSERIAL PRIMARY KEY,
                           tracking_number VARCHAR(64) NOT NULL UNIQUE,
                           sender_city VARCHAR(100) NOT NULL,
                           recipient_city VARCHAR(100) NOT NULL,
                           status VARCHAR(32) NOT NULL,
                           created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                           updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE shipment_events (
                                 id BIGSERIAL PRIMARY KEY,
                                 shipment_id BIGINT NOT NULL REFERENCES shipments(id) ON DELETE CASCADE,
                                 idempotency_key VARCHAR(64) NOT NULL UNIQUE,
                                 location VARCHAR(100) NOT NULL,
                                 status VARCHAR(32) NOT NULL,
                                 note VARCHAR(255),
                                 occurred_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_shipments_tracking_number ON shipments(tracking_number);
CREATE INDEX idx_shipment_events_shipment_id ON shipment_events(shipment_id);