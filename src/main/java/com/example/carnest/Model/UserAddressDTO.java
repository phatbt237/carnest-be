package com.example.carnest.Model;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public class UserAddressDTO {

    // ===== TẠO / SỬA ĐỊA CHỈ =====
    public static class AddressRequest {
        @NotBlank(message = "Tên người nhận không được để trống")
        private String receiverName;
        @NotBlank(message = "Số điện thoại không được để trống")
        private String phone;
        @NotBlank(message = "Tỉnh/Thành phố không được để trống")
        private String province;
        @NotBlank(message = "Quận/Huyện không được để trống")
        private String district;
        private String ward;
        @NotBlank(message = "Địa chỉ chi tiết không được để trống")
        private String streetAddress;
        private Boolean isDefault;

        public AddressRequest() {}
        public String getReceiverName() { return receiverName; }
        public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getProvince() { return province; }
        public void setProvince(String province) { this.province = province; }
        public String getDistrict() { return district; }
        public void setDistrict(String district) { this.district = district; }
        public String getWard() { return ward; }
        public void setWard(String ward) { this.ward = ward; }
        public String getStreetAddress() { return streetAddress; }
        public void setStreetAddress(String streetAddress) { this.streetAddress = streetAddress; }
        public Boolean getIsDefault() { return isDefault; }
        public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }
    }

    // ===== RESPONSE =====
    public static class AddressResponse {
        private Long id;
        private String receiverName;
        private String phone;
        private String province;
        private String district;
        private String ward;
        private String streetAddress;
        private Boolean isDefault;
        private LocalDateTime createdAt;

        public AddressResponse() {}
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getReceiverName() { return receiverName; }
        public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getProvince() { return province; }
        public void setProvince(String province) { this.province = province; }
        public String getDistrict() { return district; }
        public void setDistrict(String district) { this.district = district; }
        public String getWard() { return ward; }
        public void setWard(String ward) { this.ward = ward; }
        public String getStreetAddress() { return streetAddress; }
        public void setStreetAddress(String streetAddress) { this.streetAddress = streetAddress; }
        public Boolean getIsDefault() { return isDefault; }
        public void setIsDefault(Boolean isDefault) { this.isDefault = isDefault; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }
}
