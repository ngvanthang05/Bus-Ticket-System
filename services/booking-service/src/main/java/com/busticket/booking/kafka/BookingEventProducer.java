package com.xekhach.booking.kafka;

import com.xekhach.booking.event.BookingConfirmedEvent;
import com.xekhach.booking.event.BookingCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topic.booking-created}")
    private String bookingCreatedTopic;

    @Value("${spring.kafka.topic.booking-confirmed}")
    private String bookingConfirmedTopic;

    public void publishBookingCreated(BookingCreatedEvent event) {
        kafkaTemplate.send(bookingCreatedTopic, event.getBookingId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish booking.created for bookingId={}", event.getBookingId(), ex);
                    } else {
                        log.info("Published booking.created bookingId={}", event.getBookingId());
                    }
                });
    }

    public void publishBookingConfirmed(BookingConfirmedEvent event) {
        kafkaTemplate.send(bookingConfirmedTopic, event.getBookingId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish booking.confirmed for bookingId={}", event.getBookingId(), ex);
                    } else {
                        log.info("Published booking.confirmed bookingId={}", event.getBookingId());
                    }
                });
    }
}