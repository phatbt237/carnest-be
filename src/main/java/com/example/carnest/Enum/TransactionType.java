package com.example.carnest.Enum;

public enum TransactionType {
    DEPOSIT,            // Nạp tiền vào ví
    WITHDRAWAL,         // Rút tiền ra
    SALE_INCOME,        // Thu nhập từ bán hàng
    PURCHASE,           // Chi tiền mua hàng
    ESCROW_HOLD,        // Giữ tiền trung gian
    ESCROW_RELEASE,     // Giải phóng tiền escrow cho seller
    REFUND,             // Hoàn tiền cho buyer
    COMMISSION_FEE,     // Phí hoa hồng hệ thống thu
    AUCTION_DEPOSIT     // Đặt cọc đấu giá
}
