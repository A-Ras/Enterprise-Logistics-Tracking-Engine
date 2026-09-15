package com.logitracker.trackingservice.api.dto;

import com.logitracker.trackingservice.domain.model.Shipment;
import com.logitracker.trackingservice.domain.model.ShipmentStatus;

import java.time.Instant;
import java.util.List;

public record ShipmentResponse(
        String trackingNumber,
        String senderCity,
        String recipientCity,
        ShipmentStatus status,
        Instant updatedAt,
        List<EventDto> events
) {
    public record EventDto(String location, ShipmentStatus status, String note, Instant occurredAt) {}

    public static ShipmentResponse fromDomain(Shipment shipment) {
        var eventDtos = shipment.getEvents().stream()
                .map(e -> new EventDto(e.getLocation(), e.getStatus(), e.getNote(), e.getOccurredAt()))
                .toList();

        return new ShipmentResponse(
                shipment.getTrackingNumber(),
                shipment.getSenderCity(),
                shipment.getRecipientCity(),
                shipment.getStatus(),
                shipment.getUpdatedAt(),
                eventDtos
        );
    }
}