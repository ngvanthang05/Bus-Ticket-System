package com.xekhach.booking.controller;

import com.xekhach.booking.dto.request.CreateBookingRequest;
import com.xekhach.booking.dto.request.PaymentCallbackRequest;
import com.xekhach.booking.dto.response.ApiResponse;
import com.xekhach.booking.dto.response.BookingResponse;
import com.xekhach.booking.dto.response.PageResponse;
import com.xekhach.booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Tag(name = "Booking", description = "API quản lý đặt vé và giữ ghế")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @Operation(summary = "Tạo booking mới - khóa ghế 5 phút")
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @Valid @RequestBody CreateBookingRequest request) {
        BookingResponse response = bookingService.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Giữ ghế thành công, vui lòng thanh toán trong 5 phút", response));
    }

    @PostMapping("/payment-callback")
    @Operation(summary = "Payment Service gọi khi thanh toán thành công/thất bại")
    public ResponseEntity<ApiResponse<BookingResponse>> confirmPayment(
            @Valid @RequestBody PaymentCallbackRequest request) {
        BookingResponse response = bookingService.confirmPayment(request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái booking thành công", response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy chi tiết booking theo id")
    public ResponseEntity<ApiResponse<BookingResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("OK", bookingService.getBookingById(id)));
    }

    @GetMapping
    @Operation(summary = "Tra cứu booking theo số điện thoại, có phân trang")
    public ResponseEntity<ApiResponse<PageResponse<BookingResponse>>> getByPhone(
            @RequestParam String phone,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success("OK", bookingService.getBookingsByPhone(phone, pageable)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Hủy booking (chỉ khi đang HOLD)")
    public ResponseEntity<ApiResponse<Void>> cancel(@PathVariable UUID id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.ok(ApiResponse.success("Hủy booking thành công", null));
    }
}