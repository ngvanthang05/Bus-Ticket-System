package com.xekhach.booking.service.impl;

import com.xekhach.booking.dto.request.CreateBookingRequest;
import com.xekhach.booking.dto.request.PaymentCallbackRequest;
import com.xekhach.booking.dto.response.BookingResponse;
import com.xekhach.booking.dto.response.PageResponse;
import com.xekhach.booking.entity.Booking;
import com.xekhach.booking.entity.BookingStatus;
import com.xekhach.booking.event.BookingConfirmedEvent;
import com.xekhach.booking.event.BookingCreatedEvent;
import com.xekhach.booking.exception.BookingNotFoundException;
import com.xekhach.booking.exception.InvalidBookingStateException;
import com.xekhach.booking.exception.SeatAlreadyLockedException;
import com.xekhach.booking.kafka.BookingEventProducer;
import com.xekhach.booking.mapper.BookingMapper;
import com.xekhach.booking.redis.SeatLockService;
import com.xekhach.booking.repository.BookingRepository;
import com.xekhach.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final SeatLockService seatLockService;
    private final BookingEventProducer eventProducer;
    private final BookingMapper bookingMapper;

    /**
     * Bước 1: Khách chọn ghế -> khóa ghế trên Redis (atomic) -> tạo booking HOLD trong DB.
     * Nếu Redis lock thất bại => ghế đang bị người khác giữ => trả lỗi 409 ngay,
     * KHÔNG chạm tới DB, tránh tải không cần thiết lên DB khi tranh chấp cao.
     */
    @Override
    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request) {
        // 1. Kiểm tra DB trước (ghế đã PAID hoặc đang HOLD hợp lệ chưa hết hạn)
        bookingRepository.findByTripIdAndSeatNumberAndStatusIn(
                        request.getTripId(), request.getSeatNumber(),
                        List.of(BookingStatus.HOLD, BookingStatus.PAID))
                .ifPresent(b -> {
                    throw new SeatAlreadyLockedException(
                            "Ghế " + request.getSeatNumber() + " đang được giữ hoặc đã được đặt");
                });

        // 2. Sinh trước bookingId để dùng làm value cho Redis lock (trace được ai đang giữ ghế)
        UUID bookingId = UUID.randomUUID();

        // 3. Atomic lock trên Redis - đây là lớp chặn double-booking chính
        boolean locked = seatLockService.tryLockSeat(request.getTripId(), request.getSeatNumber(), bookingId.toString());
        if (!locked) {
            throw new SeatAlreadyLockedException(
                    "Ghế " + request.getSeatNumber() + " vừa được người khác giữ, vui lòng chọn ghế khác");
        }

        try {
            // 4. Lưu DB - nếu 2 transaction cùng lọt qua bước Redis (trường hợp cực hiếm khi Redis vừa restart),
            // unique index (trip_id, seat_number) WHERE status IN (HOLD, PAID) sẽ chặn ở đây,
            // và GlobalExceptionHandler sẽ bắt DataIntegrityViolationException -> trả 409.
            Booking booking = Booking.builder()
                    .id(bookingId)
                    .customerName(request.getCustomerName())
                    .phone(request.getPhone())
                    .tripId(request.getTripId())
                    .seatNumber(request.getSeatNumber())
                    .status(BookingStatus.HOLD)
                    .build();

            Booking saved = bookingRepository.save(booking);
            log.info("Booking created (HOLD) id={} trip={} seat={}", saved.getId(), saved.getTripId(), saved.getSeatNumber());

            // 5. Publish Kafka event để Payment Service biết mà tạo payment intent
            eventProducer.publishBookingCreated(BookingCreatedEvent.builder()
                    .bookingId(saved.getId())
                    .tripId(saved.getTripId())
                    .seatNumber(saved.getSeatNumber())
                    .customerName(saved.getCustomerName())
                    .phone(saved.getPhone())
                    .amount(saved.getAmount())
                    .build());

            return bookingMapper.toResponse(saved);
        } catch (RuntimeException ex) {
            // Rollback lock Redis nếu lưu DB thất bại, tránh ghế bị "kẹt" oan trong 5 phút
            seatLockService.releaseSeat(request.getTripId(), request.getSeatNumber());
            throw ex;
        }
    }

    /**
     * Bước 2: Payment Service gọi callback này khi thanh toán thành công.
     * Chỉ khi có callback success=true, booking mới chuyển từ HOLD -> PAID.
     */
    @Override
    @Transactional
    public BookingResponse confirmPayment(PaymentCallbackRequest request) {
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new BookingNotFoundException("Không tìm thấy booking: " + request.getBookingId()));

        if (booking.getStatus() != BookingStatus.HOLD) {
            throw new InvalidBookingStateException(
                    "Booking không ở trạng thái HOLD, không thể confirm (hiện tại: " + booking.getStatus() + ")");
        }

        if (!Boolean.TRUE.equals(request.getSuccess())) {
            // Thanh toán fail -> hủy booking, nhả ghế ngay
            booking.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);
            seatLockService.releaseSeat(booking.getTripId(), booking.getSeatNumber());
            log.info("Payment failed -> booking cancelled id={}", booking.getId());
            return bookingMapper.toResponse(booking);
        }

        booking.setStatus(BookingStatus.PAID);
        booking.setAmount(request.getAmount());
        Booking saved = bookingRepository.save(booking); // @Version tự check optimistic lock

        // Ghế đã PAID -> không cần xóa Redis lock ngay, để TTL tự hết,
        // hoặc có thể release sớm vì DB đã có unique index bảo vệ trạng thái PAID rồi.
        seatLockService.releaseSeat(saved.getTripId(), saved.getSeatNumber());

        eventProducer.publishBookingConfirmed(BookingConfirmedEvent.builder()
                .bookingId(saved.getId())
                .tripId(saved.getTripId())
                .seatNumber(saved.getSeatNumber())
                .phone(saved.getPhone())
                .build());

        log.info("Booking confirmed (PAID) id={}", saved.getId());
        return bookingMapper.toResponse(saved);
    }

    @Override
    public BookingResponse getBookingById(UUID id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Không tìm thấy booking: " + id));
        return bookingMapper.toResponse(booking);
    }

    @Override
    public PageResponse<BookingResponse> getBookingsByPhone(String phone, Pageable pageable) {
        Page<Booking> page = bookingRepository.findByPhone(phone, pageable);
        return PageResponse.from(page.map(bookingMapper::toResponse));
    }

    @Override
    @Transactional
    public void cancelBooking(UUID id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Không tìm thấy booking: " + id));

        if (booking.getStatus() == BookingStatus.PAID) {
            throw new InvalidBookingStateException("Booking đã thanh toán, không thể hủy trực tiếp (cần refund)");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        seatLockService.releaseSeat(booking.getTripId(), booking.getSeatNumber());
        log.info("Booking cancelled id={}", id);
    }
}