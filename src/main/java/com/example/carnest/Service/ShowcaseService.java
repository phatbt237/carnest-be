package com.example.carnest.Service;

import com.example.carnest.Entity.*;
import com.example.carnest.Exception.BadRequestException;
import com.example.carnest.Exception.ResourceNotFoundException;
import com.example.carnest.Model.ShopDTO;
import com.example.carnest.Model.ShowcaseDTO;
import com.example.carnest.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ShowcaseService {

    @Autowired private ShowcaseRepository showcaseRepository;
    @Autowired private ShowcaseItemRepository showcaseItemRepository;
    @Autowired private ShowcaseLikeRepository showcaseLikeRepository;
    @Autowired private UserRepository userRepository;

    @Transactional
    public ShowcaseDTO.ShowcaseResponse create(Long userId, ShowcaseDTO.CreateRequest request) {
        User user = userRepository.findById(userId).orElseThrow();
        Showcase sc = new Showcase();
        sc.setUser(user);
        sc.setName(request.getName());
        sc.setDescription(request.getDescription());
        sc.setCoverImageUrl(request.getCoverImageUrl());
        sc.setIsPublic(request.getIsPublic() != null ? request.getIsPublic() : true);
        sc = showcaseRepository.save(sc);
        return toResponse(sc, false);
    }

    @Transactional
    public ShowcaseDTO.ItemResponse addItem(Long userId, Long showcaseId, ShowcaseDTO.AddItemRequest request) {
        Showcase sc = showcaseRepository.findById(showcaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Showcase", "id", showcaseId));
        if (!sc.getUser().getId().equals(userId)) throw new BadRequestException("Không có quyền");

        ShowcaseItem item = new ShowcaseItem();
        item.setShowcase(sc);
        item.setName(request.getName());
        item.setBrand(request.getBrand());
        item.setScale(request.getScale());
        item.setImageUrl(request.getImageUrl());
        item.setDescription(request.getDescription());
        item.setPurchasePrice(request.getPurchasePrice());
        showcaseItemRepository.save(item);

        sc.setItemCount(sc.getItemCount() + 1);
        showcaseRepository.save(sc);

        ShowcaseDTO.ItemResponse res = new ShowcaseDTO.ItemResponse();
        res.setId(item.getId()); res.setName(item.getName()); res.setBrand(item.getBrand());
        res.setScale(item.getScale()); res.setImageUrl(item.getImageUrl());
        res.setDescription(item.getDescription()); res.setPurchasePrice(item.getPurchasePrice());
        return res;
    }

    @Transactional
    public String toggleLike(Long userId, Long showcaseId) {
        Showcase sc = showcaseRepository.findById(showcaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Showcase", "id", showcaseId));
        Optional<ShowcaseLike> existing = showcaseLikeRepository.findByShowcaseIdAndUserId(showcaseId, userId);
        if (existing.isPresent()) {
            showcaseLikeRepository.delete(existing.get());
            sc.setLikeCount(Math.max(0, sc.getLikeCount() - 1));
            showcaseRepository.save(sc);
            return "Đã bỏ thích";
        } else {
            ShowcaseLike like = new ShowcaseLike();
            like.setShowcase(sc);
            like.setUser(userRepository.findById(userId).orElseThrow());
            showcaseLikeRepository.save(like);
            sc.setLikeCount(sc.getLikeCount() + 1);
            showcaseRepository.save(sc);
            return "Đã thích";
        }
    }

    private static final int MAX_SIZE = 50;

    public ShopDTO.CursorPage<Map<String, Object>> getPublicShowcases(String sortBy, String cursor, int size, Long currentUserId) {
        size = Math.min(Math.max(size, 1), MAX_SIZE);
        Pageable pageable = PageRequest.of(0, size + 1);

        List<Showcase> showcases;
        if ("popular".equals(sortBy)) {
            if (cursor == null || cursor.isEmpty()) {
                showcases = showcaseRepository.findPublicPopular(pageable);
            } else {
                String[] parts = cursor.split("_");
                showcases = showcaseRepository.findPublicPopularAfterCursor(
                        Integer.parseInt(parts[0]), Long.parseLong(parts[1]), pageable);
            }
        } else if ("mostViewed".equals(sortBy)) {
            if (cursor == null || cursor.isEmpty()) {
                showcases = showcaseRepository.findPublicMostViewed(pageable);
            } else {
                String[] parts = cursor.split("_");
                showcases = showcaseRepository.findPublicMostViewedAfterCursor(
                        Integer.parseInt(parts[0]), Long.parseLong(parts[1]), pageable);
            }
        } else {
            if (cursor == null || cursor.isEmpty()) {
                showcases = showcaseRepository.findPublicNewest(pageable);
            } else {
                showcases = showcaseRepository.findPublicNewestAfterCursor(Long.parseLong(cursor), pageable);
            }
        }

        boolean hasMore = showcases.size() > size;
        if (hasMore) showcases = showcases.subList(0, size);

        List<Map<String, Object>> items = showcases.stream().map(sc -> {
            boolean isLiked = currentUserId != null && showcaseLikeRepository.existsByShowcaseIdAndUserId(sc.getId(), currentUserId);
            return toMap(sc, isLiked);
        }).collect(Collectors.toList());

        String nextCursor = null;
        if (hasMore && !showcases.isEmpty()) {
            Showcase last = showcases.get(showcases.size() - 1);
            if ("popular".equals(sortBy)) {
                nextCursor = last.getLikeCount() + "_" + last.getId();
            } else if ("mostViewed".equals(sortBy)) {
                nextCursor = last.getViewCount() + "_" + last.getId();
            } else {
                nextCursor = String.valueOf(last.getId());
            }
        }

        Long total = showcaseRepository.countPublic();
        return new ShopDTO.CursorPage<>(items, nextCursor, hasMore, items.size(), total);
    }

    public List<Map<String, Object>> getUserShowcases(Long userId) {
        return showcaseRepository.findByUserIdOrderBySortOrderAsc(userId).stream()
                .map(sc -> toMap(sc, false)).collect(Collectors.toList());
    }

    public Map<String, Object> getById(Long id, Long currentUserId) {
        Showcase sc = showcaseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Showcase", "id", id));
        Map<String, Object> m = toMap(sc, currentUserId != null && showcaseLikeRepository.existsByShowcaseIdAndUserId(id, currentUserId));
        List<ShowcaseItem> items = showcaseItemRepository.findByShowcaseIdOrderBySortOrderAsc(id);
        m.put("items", items.stream().map(i -> {
            Map<String, Object> im = new HashMap<>();
            im.put("id", i.getId()); im.put("name", i.getName()); im.put("brand", i.getBrand());
            im.put("scale", i.getScale()); im.put("imageUrl", i.getImageUrl());
            return im;
        }).collect(Collectors.toList()));
        return m;
    }

    private Map<String, Object> toMap(Showcase sc, boolean isLiked) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", sc.getId()); m.put("name", sc.getName()); m.put("description", sc.getDescription());
        m.put("coverImageUrl", sc.getCoverImageUrl()); m.put("itemCount", sc.getItemCount());
        m.put("likeCount", sc.getLikeCount()); m.put("viewCount", sc.getViewCount());
        m.put("isPublic", sc.getIsPublic()); m.put("isLiked", isLiked);
        m.put("ownerUsername", sc.getUser().getUsername()); m.put("createdAt", sc.getCreatedAt());
        return m;
    }

    private ShowcaseDTO.ShowcaseResponse toResponse(Showcase sc, boolean isLiked) {
        ShowcaseDTO.ShowcaseResponse r = new ShowcaseDTO.ShowcaseResponse();
        r.setId(sc.getId()); r.setName(sc.getName()); r.setDescription(sc.getDescription());
        r.setCoverImageUrl(sc.getCoverImageUrl()); r.setItemCount(sc.getItemCount());
        r.setLikeCount(sc.getLikeCount()); r.setViewCount(sc.getViewCount());
        r.setIsPublic(sc.getIsPublic()); r.setIsLiked(isLiked);
        r.setOwnerUsername(sc.getUser().getUsername()); r.setCreatedAt(sc.getCreatedAt());
        return r;
    }
}