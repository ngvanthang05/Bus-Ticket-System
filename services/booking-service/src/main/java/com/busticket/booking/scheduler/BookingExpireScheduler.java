package com.xekhach.booking.scheduler;

import com.xekhach.booking.entity.Booking;
import com.xekhach.booking.entity.BookingStatus;
import com.xekhach.booking.redis.SeatLockService;
import com.xekhach.booking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Redis TTL tự nhả ghế sau 5 phút, nhưng record trong DB vẫn ở trạng thái HOLD
 * cho tới khi job này chạy để đồng bộ trạng thái (đổi thành EXPIRED).
 * Chạy mỗi 60 giây.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BookingExpireScheduler {

    private final BookingRepository bookingRepository;
    private final SeatLockService seatLockService;

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void expireStaleHoldBookings() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(5);
        List<Booking> staleBookings = bookingRepository.findByStatusAndCreatedAtBefore(BookingStatus.HOLD, threshold);

        for (Booking booking : staleBookings) {
            booking.setStatus(BookingStatus.EXPIRED);
            bookingRepository.save(booking);
            seatLockService.releaseSeat(booking.getTripId(), booking.getSeatNumber());
            log.info("Booking expired id={}", booking.getId());
        }

        if (!staleBookings.isEmpty()) {
            log.info("Expired {} stale HOLD bookings", staleBookings.size());
        }
    }
}