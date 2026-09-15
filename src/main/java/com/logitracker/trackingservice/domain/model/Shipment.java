package com.logitracker.trackingservice.domain.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "shipments")
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tracking_number", nullable = false, unique = true)
    private String trackingNumber;

    @Column(name = "sender_city", nullable = false)
    private String senderCity;

    @Column(name = "recipient_city", nullable = false)
    private String recipientCity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ShipmentStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShipmentEvent> events = new ArrayList<>();

    // Standard-Konstruktor für JPA
    protected Shipment() {}

    public Shipment(String trackingNumber, String senderCity, String recipientCity) {
        this.trackingNumber = trackingNumber;
        this.senderCity = senderCity;
        this.recipientCity = recipientCity;
        this.status = ShipmentStatus.CREATED;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    // Helper-Methode für Business-Logik
    public void addEvent(ShipmentEvent event) {
        events.add(event);
        event.setShipment(this);
        this.status = event.getStatus();
        this.updatedAt = Instant.now();
    }

    // Getter
    public Long getId() { return id; }
    public String getTrackingNumber() { return trackingNumber; }
    public String getSenderCity() { return senderCity; }
    public String getRecipientCity() { return recipientCity; }
    public ShipmentStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public List<ShipmentEvent> getEvents() { return events; }
}