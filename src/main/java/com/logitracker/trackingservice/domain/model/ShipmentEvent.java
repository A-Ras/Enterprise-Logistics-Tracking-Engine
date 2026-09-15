package com.logitracker.trackingservice.domain.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "shipment_events")
public class ShipmentEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    @Column(name = "location", nullable = false)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ShipmentStatus status;

    @Column(name = "note")
    private String note;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected ShipmentEvent() {}

    public ShipmentEvent(String idempotencyKey, String location, ShipmentStatus status, String note) {
        this.idempotencyKey = idempotencyKey;
        this.location = location;
        this.status = status;
        this.note = note;
        this.occurredAt = Instant.now();
    }

    // Getter & Setter
    public Long getId() { return id; }
    public Shipment getShipment() { return shipment; }
    public void setShipment(Shipment shipment) { this.shipment = shipment; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public String getLocation() { return location; }
    public ShipmentStatus getStatus() { return status; }
    public String getNote() { return note; }
    public Instant getOccurredAt() { return occurredAt; }
}