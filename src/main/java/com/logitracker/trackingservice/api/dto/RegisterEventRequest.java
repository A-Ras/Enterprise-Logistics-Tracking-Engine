package com.logitracker.trackingservice.api.dto;

import com.logitracker.trackingservice.domain.model.ShipmentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterEventRequest(
        @NotBlank(message = "Idempotency-Key ist erforderlich")
        String idempotencyKey,

        @NotBlank(message = "Standort ist erforderlich")
        String location,

        @NotNull(message = "Status ist erforderlich")
        ShipmentStatus status,

        String note
) {}