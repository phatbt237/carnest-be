package com.example.carnest.Enum;

public enum EscrowStatus {
    NONE,       // Không dùng escrow (COD)
    HOLDING,    // Đang giữ tiền
    RELEASED,   // Đã chuyển cho seller
    REFUNDED    // Đã hoàn cho buyer
}
