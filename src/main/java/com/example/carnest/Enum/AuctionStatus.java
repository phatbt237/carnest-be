package com.example.carnest.Enum;

public enum AuctionStatus {
    UPCOMING,   // Chưa bắt đầu
    ACTIVE,     // Đang diễn ra
    ENDED,      // Đã kết thúc (chờ xử lý)
    COMPLETED,  // Hoàn thành (đã tạo đơn)
    CANCELLED,  // Đã hủy
    NO_SALE     // Không bán (bid < reserve price)
}
