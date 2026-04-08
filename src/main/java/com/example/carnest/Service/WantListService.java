package com.example.carnest.Service;

import com.example.carnest.Entity.WantList;
import com.example.carnest.Enum.WantListStatus;
import com.example.carnest.Exception.BadRequestException;
import com.example.carnest.Exception.ResourceNotFoundException;
import com.example.carnest.Model.ShopDTO;
import com.example.carnest.Model.WantListDTO;
import com.example.carnest.Repository.UserRepository;
import com.example.carnest.Repository.WantListRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WantListService {

    @Autowired private WantListRepository wantListRepository;
    @Autowired private UserRepository userRepository;

    private static final int MAX_SIZE = 50;

    @Transactional
    public WantListDTO.WantListResponse create(Long userId, WantListDTO.CreateRequest request) {
        WantList w = new WantList();
        w.setUser(userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId)));
        w.setTitle(request.getTitle().trim());
        w.setDescription(request.getDescription());
        w.setScale(request.getScale());
        w.setCarBrand(request.getCarBrand());
        w.setCarModel(request.getCarModel());
        w.setMaxPrice(request.getMaxPrice());
        w.setStatus(WantListStatus.ACTIVE);
        w.setIsPublic(request.getIsPublic() != null ? request.getIsPublic() : true);
        w.setAutoNotify(true);
        w = wantListRepository.save(w);
        return toResponse(w);
    }

    @Transactional
    public WantListDTO.WantListResponse update(Long userId, Long id, WantListDTO.CreateRequest request) {
        WantList w = wantListRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WantList", "id", id));
        if (!w.getUser().getId().equals(userId)) {
            throw new BadRequestException("Bạn không có quyền sửa yêu cầu này");
        }

        if (request.getTitle() != null) w.setTitle(request.getTitle().trim());
        if (request.getDescription() != null) w.setDescription(request.getDescription());
        if (request.getScale() != null) w.setScale(request.getScale());
        if (request.getCarBrand() != null) w.setCarBrand(request.getCarBrand());
        if (request.getCarModel() != null) w.setCarModel(request.getCarModel());
        if (request.getMaxPrice() != null) w.setMaxPrice(request.getMaxPrice());
        if (request.getIsPublic() != null) w.setIsPublic(request.getIsPublic());

        w = wantListRepository.save(w);
        return toResponse(w);
    }

    @Transactional
    public void cancel(Long userId, Long id) {
        WantList w = wantListRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WantList", "id", id));
        if (!w.getUser().getId().equals(userId)) {
            throw new BadRequestException("Bạn không có quyền hủy yêu cầu này");
        }
        w.setStatus(WantListStatus.CANCELLED);
        wantListRepository.save(w);
    }

    public ShopDTO.CursorPage<WantListDTO.WantListResponse> getMyWantList(
            Long userId, String cursor, int size) {
        size = Math.min(Math.max(size, 1), MAX_SIZE);
        Long cursorId = (cursor != null && !cursor.isEmpty()) ? Long.parseLong(cursor) : null;

        List<WantList> items = wantListRepository.findByUserId(userId, cursorId, size + 1);
        boolean hasMore = items.size() > size;
        if (hasMore) items = items.subList(0, size);

        List<WantListDTO.WantListResponse> responses = items.stream()
                .map(this::toResponse).collect(Collectors.toList());
        String nextCursor = hasMore && !items.isEmpty()
                ? String.valueOf(items.get(items.size() - 1).getId()) : null;

        return new ShopDTO.CursorPage<>(responses, nextCursor, hasMore,
                responses.size(), wantListRepository.countByUserId(userId));
    }

    public ShopDTO.CursorPage<WantListDTO.WantListResponse> getPublicWantList(
            String cursor, int size) {
        size = Math.min(Math.max(size, 1), MAX_SIZE);
        Long cursorId = (cursor != null && !cursor.isEmpty()) ? Long.parseLong(cursor) : null;

        List<WantList> items = wantListRepository.findPublicActive(cursorId, size + 1);
        boolean hasMore = items.size() > size;
        if (hasMore) items = items.subList(0, size);

        List<WantListDTO.WantListResponse> responses = items.stream()
                .map(this::toResponse).collect(Collectors.toList());
        String nextCursor = hasMore && !items.isEmpty()
                ? String.valueOf(items.get(items.size() - 1).getId()) : null;

        return new ShopDTO.CursorPage<>(responses, nextCursor, hasMore, responses.size(), null);
    }

    private WantListDTO.WantListResponse toResponse(WantList w) {
        WantListDTO.WantListResponse r = new WantListDTO.WantListResponse();
        r.setId(w.getId());
        r.setTitle(w.getTitle());
        r.setDescription(w.getDescription());
        r.setScale(w.getScale());
        r.setCarBrand(w.getCarBrand());
        r.setCarModel(w.getCarModel());
        r.setMaxPrice(w.getMaxPrice());
        r.setStatus(w.getStatus().name());
        r.setIsPublic(w.getIsPublic());
        r.setUsername(w.getUser().getUsername());
        r.setCreatedAt(w.getCreatedAt());
        return r;
    }
}