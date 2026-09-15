package com.logitracker.trackingservice.domain.model;

import java.time.Instant;

public record ShipmentStatusChangedEvent(
        String trackingNumber,
        ShipmentStatus oldStatus,
        ShipmentStatus newStatus,
        String location,
        Instant timestamp
) {}
