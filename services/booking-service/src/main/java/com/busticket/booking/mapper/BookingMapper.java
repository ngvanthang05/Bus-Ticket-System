package com.xekhach.booking.mapper;

import com.xekhach.booking.dto.response.BookingResponse;
import com.xekhach.booking.entity.Booking;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BookingMapper {
    BookingResponse toResponse(Booking booking);
}