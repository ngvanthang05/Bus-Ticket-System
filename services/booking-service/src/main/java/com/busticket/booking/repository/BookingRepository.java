package com.xekhach.booking.repository;

import com.xekhach.booking.entity.Booking;
import com.xekhach.booking.entity.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    Optional<Booking> findByTripIdAndSeatNumberAndStatusIn(UUID tripId, String seatNumber, List<BookingStatus> statuses);

    Page<Booking> findByPhone(String phone, Pageable pageable);

    Page<Booking> findByTripId(UUID tripId, Pageable pageable);

    List<Booking> findByStatusAndCreatedAtBefore(BookingStatus status, LocalDateTime threshold);
}