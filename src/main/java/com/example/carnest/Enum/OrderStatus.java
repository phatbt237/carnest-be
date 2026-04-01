package com.example.carnest.Enum;

public enum OrderStatus {
    PENDING_PAYMENT,    // Chờ thanh toán
    PENDING,            // Chờ xác nhận
    CONFIRMED,          // Đã xác nhận
    SHIPPING,           // Đang vận chuyển
    DELIVERED,          // Đã giao hàng
    COMPLETED,          // Hoàn thành
    CANCELLED,          // Đã hủy
    EXPIRED,            // Hết hạn
    RETURN_REQUESTED,   // Yêu cầu đổi trả
    RETURNED,           // Đã trả hàng
    REFUNDED            // Đã hoàn tiền
}