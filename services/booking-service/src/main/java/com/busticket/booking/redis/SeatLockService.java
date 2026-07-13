package com.xekhach.booking.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

/**
 * Xử lý khóa ghế bằng Redis.
 * Dùng lệnh SETNX (SET key value NX EX) - đây là lệnh ATOMIC ở tầng Redis server,
 * nên dù hàng nghìn request cùng gọi song song, chỉ đúng 1 request nhận được lock.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeatLockService {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${booking.seat-lock-ttl-seconds:300}")
    private long lockTtlSeconds;

    private String buildKey(UUID tripId, String seatNumber) {
        return "seat_lock:%s:%s".formatted(tripId, seatNumber);
    }

    /**
     * Cố gắng khóa ghế. Trả về true nếu khóa thành công (ghế đang trống),
     * false nếu ghế đã bị người khác giữ.
     */
    public boolean tryLockSeat(UUID tripId, String seatNumber, String bookingId) {
        String key = buildKey(tripId, seatNumber);
        Boolean locked = redisTemplate.opsForValue()
                .setIfAbsent(key, bookingId, Duration.ofSeconds(lockTtlSeconds));
        boolean success = Boolean.TRUE.equals(locked);
        log.info("Seat lock attempt key={} success={}", key, success);
        return success;
    }

    /** Giải phóng ghế (khi hủy booking hoặc thanh toán fail). */
    public void releaseSeat(UUID tripId, String seatNumber) {
        String key = buildKey(tripId, seatNumber);
        redisTemplate.delete(key);
        log.info("Seat released key={}", key);
    }

    /** Kiểm tra ghế đang bị khóa bởi bookingId nào (dùng để xác nhận trước khi confirm). */
    public String getLockOwner(UUID tripId, String seatNumber) {
        return redisTemplate.opsForValue().get(buildKey(tripId, seatNumber));
    }
}