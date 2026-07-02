package com.example.carnest.API;

import com.example.carnest.Config.CustomUserDetails;
import com.example.carnest.Model.AuthDTO;
import com.example.carnest.Model.UserAddressDTO;
import com.example.carnest.Service.UserAddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/addresses")
@Tag(name = "UserAddress", description = "Sổ địa chỉ giao hàng của tôi (tối đa 5)")
public class UserAddressController {

    @Autowired private UserAddressService userAddressService;

    @GetMapping
    @Operation(summary = "Danh sách địa chỉ của tôi")
    public ResponseEntity<AuthDTO.MessageResponse> getMyAddresses(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Thành công")
                .data(userAddressService.getMyAddresses(userDetails.getUserId())).build());
    }

    @PostMapping
    @Operation(summary = "Thêm địa chỉ mới", description = "Tối đa 5 địa chỉ/người dùng. Địa chỉ đầu tiên tự động là mặc định.")
    public ResponseEntity<AuthDTO.MessageResponse> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UserAddressDTO.AddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AuthDTO.MessageResponse.builder()
                        .status(201).message("Đã thêm địa chỉ")
                        .data(userAddressService.create(userDetails.getUserId(), request)).build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Sửa địa chỉ")
    public ResponseEntity<AuthDTO.MessageResponse> update(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @RequestBody UserAddressDTO.AddressRequest request) {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Đã cập nhật địa chỉ")
                .data(userAddressService.update(userDetails.getUserId(), id, request)).build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xoá địa chỉ", description = "Nếu xoá địa chỉ mặc định, địa chỉ mới nhất còn lại sẽ tự động thành mặc định")
    public ResponseEntity<AuthDTO.MessageResponse> delete(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        userAddressService.delete(userDetails.getUserId(), id);
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Đã xoá địa chỉ").build());
    }

    @PutMapping("/{id}/default")
    @Operation(summary = "Đặt làm địa chỉ mặc định")
    public ResponseEntity<AuthDTO.MessageResponse> setDefault(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder()
                .status(200).message("Đã đặt làm địa chỉ mặc định")
                .data(userAddressService.setDefault(userDetails.getUserId(), id)).build());
    }
}
