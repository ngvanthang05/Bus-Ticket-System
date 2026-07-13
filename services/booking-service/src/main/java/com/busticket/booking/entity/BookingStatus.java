package com.xekhach.booking.entity;

public enum BookingStatus {
    HOLD,       // đang giữ ghế, chờ thanh toán
    PAID,       // đã thanh toán, xác nhận booking
    CANCELLED,  // hết hạn giữ ghế hoặc khách hủy
    EXPIRED     // hết 5 phút mà chưa thanh toán
}