package com.logitracker.trackingservice.infrastructure.messaging;

import com.logitracker.trackingservice.domain.model.ShipmentStatusChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class ShipmentNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(ShipmentNotificationListener.class);

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleStatusChanged(ShipmentStatusChangedEvent event) {
        log.info("📨 [NOTIFICATION SERVICE] Benachrichtigung für Sendung {} empfangen!", event.trackingNumber());
        log.info("Status geändert von {} auf {} an Standort: {}",
                event.oldStatus(), event.newStatus(), event.location());
        // Hier würde z.B. eine E-Mail an den Empfänger versendet werden
    }
}