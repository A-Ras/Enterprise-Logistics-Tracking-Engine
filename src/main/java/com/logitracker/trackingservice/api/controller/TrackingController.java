package com.logitracker.trackingservice.api.controller;

import com.logitracker.trackingservice.api.dto.CreateShipmentRequest;
import com.logitracker.trackingservice.api.dto.RegisterEventRequest;
import com.logitracker.trackingservice.api.dto.ShipmentResponse;
import com.logitracker.trackingservice.service.TrackingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/shipments")
public class TrackingController {

    private final TrackingService trackingService;

    public TrackingController(TrackingService trackingService) {
        this.trackingService = trackingService;
    }

    @PostMapping
    public ResponseEntity<ShipmentResponse> createShipment(@Valid @RequestBody CreateShipmentRequest request) {
        ShipmentResponse created = trackingService.createShipment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{trackingNumber}")
    public ResponseEntity<ShipmentResponse> getShipment(@PathVariable String trackingNumber) {
        return ResponseEntity.ok(trackingService.getShipment(trackingNumber));
    }

    @PostMapping("/{trackingNumber}/events")
    public ResponseEntity<ShipmentResponse> registerEvent(
            @PathVariable String trackingNumber,
            @Valid @RequestBody RegisterEventRequest request) {
        ShipmentResponse updated = trackingService.registerEvent(trackingNumber, request);
        return ResponseEntity.ok(updated);
    }
}