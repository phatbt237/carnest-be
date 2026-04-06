package com.example.carnest.API;

import com.example.carnest.Config.CustomUserDetails;
import com.example.carnest.Entity.WantList;
import com.example.carnest.Enum.WantListStatus;
import com.example.carnest.Exception.ResourceNotFoundException;
import com.example.carnest.Model.AuthDTO;
import com.example.carnest.Model.ShopDTO;
import com.example.carnest.Repository.UserRepository;
import com.example.carnest.Repository.WantListRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/wantlist")
@Tag(name = "WantList", description = "Tìm xe hộ tôi")
public class WantListController {

    @Autowired private WantListRepository wantListRepository;
    @Autowired private UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Đăng yêu cầu tìm xe")
    public ResponseEntity<AuthDTO.MessageResponse> create(
            @AuthenticationPrincipal CustomUserDetails u, @RequestBody Map<String, Object> body) {
        WantList w = new WantList();
        w.setUser(userRepository.findById(u.getUserId()).orElseThrow());
        w.setTitle((String) body.get("title"));
        w.setDescription((String) body.get("description"));
        w.setScale((String) body.get("scale"));
        w.setCarBrand((String) body.get("carBrand"));
        w.setCarModel((String) body.get("carModel"));
        if (body.get("maxPrice") != null) w.setMaxPrice(new BigDecimal(body.get("maxPrice").toString()));
        w.setStatus(WantListStatus.ACTIVE);
        w.setIsPublic(body.get("isPublic") != null ? (Boolean) body.get("isPublic") : true);
        w.setAutoNotify(true);
        w = wantListRepository.save(w);
        return ResponseEntity.status(HttpStatus.CREATED).body(AuthDTO.MessageResponse.builder()
                .status(201).message("Đăng yêu cầu thành công").data(toMap(w)).build());
    }

    @GetMapping("/my")
    @Operation(summary = "Yêu cầu tìm xe của tôi")
    public ResponseEntity<AuthDTO.MessageResponse> my(
            @AuthenticationPrincipal CustomUserDetails u,
            @RequestParam(required = false) String cursor, @RequestParam(defaultValue = "20") int size) {
        Long cursorId = (cursor != null && !cursor.isEmpty()) ? Long.parseLong(cursor) : null;
        List<WantList> items = wantListRepository.findByUserId(u.getUserId(), cursorId, size + 1);
        boolean hasMore = items.size() > size;
        if (hasMore) items = items.subList(0, size);
        String nextCursor = hasMore && !items.isEmpty() ? String.valueOf(items.get(items.size() - 1).getId()) : null;
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder().status(200).message("Thành công")
                .data(new ShopDTO.CursorPage<>(items.stream().map(this::toMap).collect(Collectors.toList()),
                        nextCursor, hasMore, items.size(), wantListRepository.countByUserId(u.getUserId()))).build());
    }

    @GetMapping("/public")
    @Operation(summary = "Yêu cầu tìm xe công khai (public)")
    public ResponseEntity<AuthDTO.MessageResponse> publicList(
            @RequestParam(required = false) String cursor, @RequestParam(defaultValue = "20") int size) {
        Long cursorId = (cursor != null && !cursor.isEmpty()) ? Long.parseLong(cursor) : null;
        List<WantList> items = wantListRepository.findPublicActive(cursorId, size + 1);
        boolean hasMore = items.size() > size;
        if (hasMore) items = items.subList(0, size);
        String nextCursor = hasMore && !items.isEmpty() ? String.valueOf(items.get(items.size() - 1).getId()) : null;
        return ResponseEntity.ok(AuthDTO.MessageResponse.builder().status(200).message("Thành công")
                .data(new ShopDTO.CursorPage<>(items.stream().map(this::toMap).collect(Collectors.toList()),
                        nextCursor, hasMore, items.size(), null)).build());
    }

    private Map<String, Object> toMap(WantList w) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", w.getId()); m.put("title", w.getTitle()); m.put("description", w.getDescription());
        m.put("scale", w.getScale()); m.put("carBrand", w.getCarBrand()); m.put("carModel", w.getCarModel());
        m.put("maxPrice", w.getMaxPrice()); m.put("status", w.getStatus().name());
        m.put("isPublic", w.getIsPublic()); m.put("username", w.getUser().getUsername());
        m.put("createdAt", w.getCreatedAt());
        return m;
    }
}