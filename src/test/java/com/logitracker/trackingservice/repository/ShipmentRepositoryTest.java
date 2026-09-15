package com.logitracker.trackingservice.repository;

import com.logitracker.trackingservice.domain.model.Shipment;
import com.logitracker.trackingservice.domain.model.ShipmentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ShipmentRepositoryTest {

    // Startet automatisch einen echten Postgres-Container für den Test!
    // @ServiceConnection verdrahtet Host/Port/Credentials magisch mit Spring Boot 3.1+
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    private final ShipmentRepository shipmentRepository;
    @Autowired
    ShipmentRepositoryTest(ShipmentRepository shipmentRepository) {
        this.shipmentRepository = shipmentRepository;
    }

    @Test
    @DisplayName("Sollte eine Sendung anhand der Tracking-Nummer finden")
    void shouldFindShipmentByTrackingNumber() {
        // Given (Arrange)
        String trackingNumber = "HH-TRACK-12345";
        Shipment shipment = new Shipment(trackingNumber, "Hamburg", "München");
        shipmentRepository.save(shipment);

        // When (Act)
        Optional<Shipment> found = shipmentRepository.findByTrackingNumber(trackingNumber);

        // Then (Assert)
        assertThat(found).isPresent();
        assertThat(found.get().getSenderCity()).isEqualTo("Hamburg");
        assertThat(found.get().getRecipientCity()).isEqualTo("München");
        assertThat(found.get().getStatus()).isEqualTo(ShipmentStatus.CREATED);
    }

    @Test
    @DisplayName("Sollte erkennen, ob eine Tracking-Nummer bereits existiert")
    void shouldCheckIfTrackingNumberExists() {
        // Given
        String trackingNumber = "HH-TRACK-99999";
        shipmentRepository.save(new Shipment(trackingNumber, "Hamburg", "Berlin"));

        // When & Then
        assertThat(shipmentRepository.existsByTrackingNumber(trackingNumber)).isTrue();
        assertThat(shipmentRepository.existsByTrackingNumber("NON-EXISTENT")).isFalse();
    }
}