package com.logitracker.trackingservice.service;

import com.logitracker.trackingservice.api.dto.CreateShipmentRequest;
import com.logitracker.trackingservice.api.dto.RegisterEventRequest;
import com.logitracker.trackingservice.api.dto.ShipmentResponse;
import com.logitracker.trackingservice.domain.model.Shipment;
import com.logitracker.trackingservice.domain.model.ShipmentEvent;
import com.logitracker.trackingservice.domain.model.ShipmentStatus;
import com.logitracker.trackingservice.domain.model.ShipmentStatusChangedEvent;
import com.logitracker.trackingservice.infrastructure.messaging.RabbitMQConfig;
import com.logitracker.trackingservice.repository.ShipmentRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.NoSuchElementException;

@Service
public class TrackingService {

    private static final Logger log = LoggerFactory.getLogger(TrackingService.class);

    private final ShipmentRepository shipmentRepository;
    private final RabbitTemplate rabbitTemplate;
    private final MeterRegistry meterRegistry;
    private final Timer eventProcessingTimer;

    public TrackingService(ShipmentRepository shipmentRepository,
                           RabbitTemplate rabbitTemplate,
                           MeterRegistry meterRegistry) {
        this.shipmentRepository = shipmentRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.meterRegistry = meterRegistry;

        // Timer zur Messung der Verarbeitungszeit
        this.eventProcessingTimer = Timer.builder("shipment.event.processing.time")
                .description("Zeitdauer für die Verarbeitung eines Tracking-Events")
                .register(meterRegistry);
    }

    @Transactional
    public ShipmentResponse createShipment(CreateShipmentRequest request) {
        if (shipmentRepository.existsByTrackingNumber(request.trackingNumber())) {
            throw new IllegalArgumentException("Sendung mit dieser Tracking-Nummer existiert bereits: " + request.trackingNumber());
        }

        Shipment shipment = new Shipment(request.trackingNumber(), request.senderCity(), request.recipientCity());
        Shipment saved = shipmentRepository.save(shipment);
        return ShipmentResponse.fromDomain(saved);
    }

    @Transactional(readOnly = true)
    public ShipmentResponse getShipment(String trackingNumber) {
        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new NoSuchElementException("Sendung nicht gefunden: " + trackingNumber));
        return ShipmentResponse.fromDomain(shipment);
    }

    @Transactional
    public ShipmentResponse registerEvent(String trackingNumber, RegisterEventRequest request) {
        // Wir wickeln die Ausführung im Timer ein, um die Millisekunden zu messen
        return eventProcessingTimer.record(() -> {
            Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
                    .orElseThrow(() -> new NoSuchElementException("Sendung nicht gefunden: " + trackingNumber));

            // Idempotenz-Prüfung
            boolean alreadyProcessed = shipment.getEvents().stream()
                    .anyMatch(event -> event.getIdempotencyKey().equals(request.idempotencyKey()));

            if (alreadyProcessed) {
                // Duplikat-Metrik hochzählen
                meterRegistry.counter("shipment.events.duplicates.total").increment();
                return ShipmentResponse.fromDomain(shipment);
            }

            ShipmentStatus oldStatus = shipment.getStatus();

            ShipmentEvent newEvent = new ShipmentEvent(
                    request.idempotencyKey(),
                    request.location(),
                    request.status(),
                    request.note()
            );

            shipment.addEvent(newEvent);
            Shipment updated = shipmentRepository.save(shipment);

            // Asynchrones Event an RabbitMQ senden
            String routingKey = "shipment.status." + newEvent.getStatus().name().toLowerCase();
            ShipmentStatusChangedEvent eventPayload = new ShipmentStatusChangedEvent(
                    shipment.getTrackingNumber(),
                    oldStatus,
                    newEvent.getStatus(),
                    newEvent.getLocation(),
                    Instant.now()
            );

            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, routingKey, eventPayload);
            log.info("Event an RabbitMQ gesendet [RoutingKey: {}]: {}", routingKey, eventPayload);

            // Business-Metrik mit Tags hochzählen (für Grafana-Filterung)
            Counter.builder("shipment.events.processed.total")
                    .tag("location", newEvent.getLocation())
                    .tag("status", newEvent.getStatus().name())
                    .description("Anzahl erfolgreich verarbeiteter Scan-Events")
                    .register(meterRegistry)
                    .increment();

            return ShipmentResponse.fromDomain(updated);
        });
    }
}