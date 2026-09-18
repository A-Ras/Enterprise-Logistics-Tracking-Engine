package com.logitracker.trackingservice.api.controller;

import com.logitracker.trackingservice.api.dto.CreateShipmentRequest;
import com.logitracker.trackingservice.api.dto.RegisterEventRequest;
import com.logitracker.trackingservice.api.dto.ShipmentResponse;
import com.logitracker.trackingservice.service.TrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/shipments")
@Tag(name = "Shipment Tracking", description = "Endpunkte zum Verwalten von Sendungen und Scan-Events")
public class TrackingController {

    private final TrackingService trackingService;

    public TrackingController(TrackingService trackingService) {
        this.trackingService = trackingService;
    }

    @PostMapping
    @Operation(summary = "Neue Sendung registrieren", description = "Erstellt eine neue Sendung mit Ausgangs- und Zielort.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Sendung erfolgreich angelegt"),
            @ApiResponse(responseCode = "400", description = "Ungültige Eingabedaten"),
            @ApiResponse(responseCode = "409", description = "Tracking-Nummer existiert bereits")
    })
    public ResponseEntity<ShipmentResponse> createShipment(@Valid @RequestBody CreateShipmentRequest request) {
        ShipmentResponse created = trackingService.createShipment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{trackingNumber}")
    @Operation(summary = "Sendungsdetails & Historie abrufen", description = "Liefert den aktuellen Status und alle bisherigen Scan-Events.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sendung gefunden"),
            @ApiResponse(responseCode = "404", description = "Sendung nicht gefunden")
    })
    public ResponseEntity<ShipmentResponse> getShipment(@PathVariable String trackingNumber) {
        return ResponseEntity.ok(trackingService.getShipment(trackingNumber));
    }

    @PostMapping("/{trackingNumber}/events")
    @Operation(summary = "Scan-Event erfassen (Idempotent)", description = "Erfasst einen Zwischenstopp oder Statuswechsel und publisht ein Event an RabbitMQ.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event erfolgreich verbucht (oder als Duplikat ignoriert)"),
            @ApiResponse(responseCode = "404", description = "Sendung existiert nicht")
    })
    public ResponseEntity<ShipmentResponse> registerEvent(
            @PathVariable String trackingNumber,
            @Valid @RequestBody RegisterEventRequest request) {
        ShipmentResponse updated = trackingService.registerEvent(trackingNumber, request);
        return ResponseEntity.ok(updated);
    }
}