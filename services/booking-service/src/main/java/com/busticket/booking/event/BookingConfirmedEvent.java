package com.xekhach.booking.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingConfirmedEvent {
    private UUID bookingId;
    private UUID tripId;
    private String seatNumber;
    private String phone;
}