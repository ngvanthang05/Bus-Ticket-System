package com.xekhach.tripservice.entity;

public enum TripStatus {
    SCHEDULED,      // Đã tạo, chưa mở bán
    OPEN_FOR_SALE,  // Mở bán vé
    CLOSED,         // Đóng bán (hết ghế / khóa sổ trước giờ chạy)
    RUNNING,        // Đang chạy
    COMPLETED,      // Đã đến
    CANCELLED       // Hủy chuyến
}