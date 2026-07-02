package com.example.carnest.Service;

import com.example.carnest.Entity.User;
import com.example.carnest.Entity.UserAddress;
import com.example.carnest.Exception.BadRequestException;
import com.example.carnest.Exception.ResourceNotFoundException;
import com.example.carnest.Model.UserAddressDTO;
import com.example.carnest.Repository.UserAddressRepository;
import com.example.carnest.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserAddressService {

    @Autowired private UserAddressRepository userAddressRepository;
    @Autowired private UserRepository userRepository;

    public static final int MAX_ADDRESSES = 5;

    public List<UserAddressDTO.AddressResponse> getMyAddresses(Long userId) {
        return userAddressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public UserAddressDTO.AddressResponse create(Long userId, UserAddressDTO.AddressRequest request) {
        long count = userAddressRepository.countByUserId(userId);
        if (count >= MAX_ADDRESSES) {
            throw new BadRequestException("Bạn chỉ có thể lưu tối đa " + MAX_ADDRESSES + " địa chỉ giao hàng");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        boolean makeDefault = count == 0 || Boolean.TRUE.equals(request.getIsDefault());
        if (makeDefault) {
            clearCurrentDefault(userId);
        }

        UserAddress address = UserAddress.builder()
                .user(user)
                .receiverName(request.getReceiverName().trim())
                .phone(request.getPhone().trim())
                .province(request.getProvince().trim())
                .district(request.getDistrict().trim())
                .ward(request.getWard() != null ? request.getWard().trim() : null)
                .streetAddress(request.getStreetAddress().trim())
                .isDefault(makeDefault)
                .build();

        address = userAddressRepository.save(address);
        return toResponse(address);
    }

    @Transactional
    public UserAddressDTO.AddressResponse update(Long userId, Long id, UserAddressDTO.AddressRequest request) {
        UserAddress address = userAddressRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Địa chỉ", "id", id));

        if (request.getReceiverName() != null) address.setReceiverName(request.getReceiverName().trim());
        if (request.getPhone() != null) address.setPhone(request.getPhone().trim());
        if (request.getProvince() != null) address.setProvince(request.getProvince().trim());
        if (request.getDistrict() != null) address.setDistrict(request.getDistrict().trim());
        if (request.getWard() != null) address.setWard(request.getWard().trim());
        if (request.getStreetAddress() != null) address.setStreetAddress(request.getStreetAddress().trim());

        if (Boolean.TRUE.equals(request.getIsDefault()) && !Boolean.TRUE.equals(address.getIsDefault())) {
            clearCurrentDefault(userId);
            address.setIsDefault(true);
        }

        address = userAddressRepository.save(address);
        return toResponse(address);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        UserAddress address = userAddressRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Địa chỉ", "id", id));

        boolean wasDefault = Boolean.TRUE.equals(address.getIsDefault());
        userAddressRepository.delete(address);

        if (wasDefault) {
            userAddressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId)
                    .stream().findFirst()
                    .ifPresent(next -> {
                        next.setIsDefault(true);
                        userAddressRepository.save(next);
                    });
        }
    }

    @Transactional
    public UserAddressDTO.AddressResponse setDefault(Long userId, Long id) {
        UserAddress address = userAddressRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Địa chỉ", "id", id));

        if (!Boolean.TRUE.equals(address.getIsDefault())) {
            clearCurrentDefault(userId);
            address.setIsDefault(true);
            address = userAddressRepository.save(address);
        }
        return toResponse(address);
    }

    private void clearCurrentDefault(Long userId) {
        userAddressRepository.findByUserIdAndIsDefaultTrue(userId).ifPresent(current -> {
            current.setIsDefault(false);
            userAddressRepository.save(current);
        });
    }

    private UserAddressDTO.AddressResponse toResponse(UserAddress a) {
        UserAddressDTO.AddressResponse r = new UserAddressDTO.AddressResponse();
        r.setId(a.getId());
        r.setReceiverName(a.getReceiverName());
        r.setPhone(a.getPhone());
        r.setProvince(a.getProvince());
        r.setDistrict(a.getDistrict());
        r.setWard(a.getWard());
        r.setStreetAddress(a.getStreetAddress());
        r.setIsDefault(a.getIsDefault());
        r.setCreatedAt(a.getCreatedAt());
        return r;
    }
}
