package com.xekhach.booking.service;

import com.xekhach.booking.dto.request.CreateBookingRequest;
import com.xekhach.booking.dto.request.PaymentCallbackRequest;
import com.xekhach.booking.dto.response.BookingResponse;
import com.xekhach.booking.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface BookingService {

    BookingResponse createBooking(CreateBookingRequest request);

    BookingResponse confirmPayment(PaymentCallbackRequest request);

    BookingResponse getBookingById(UUID id);

    PageResponse<BookingResponse> getBookingsByPhone(String phone, Pageable pageable);

    void cancelBooking(UUID id);
}