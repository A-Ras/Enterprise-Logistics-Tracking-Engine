package com.logitracker.trackingservice.api.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateShipmentRequest(
        @NotBlank(message = "Tracking-Nummer darf nicht leer sein")
        String trackingNumber,

        @NotBlank(message = "Absendeort darf nicht leer sein")
        String senderCity,

        @NotBlank(message = "Zielort darf nicht leer sein")
        String recipientCity
) {}