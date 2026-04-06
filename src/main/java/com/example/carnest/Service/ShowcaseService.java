package com.example.carnest.Service;

import com.example.carnest.Entity.*;
import com.example.carnest.Exception.BadRequestException;
import com.example.carnest.Exception.ResourceNotFoundException;
import com.example.carnest.Model.ShopDTO;
import com.example.carnest.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
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
    public Map<String, Object> create(Long userId, Map<String, Object> request) {
        User user = userRepository.findById(userId).orElseThrow();
        Showcase sc = new Showcase();
        sc.setUser(user);
        sc.setName((String) request.get("name"));
        sc.setDescription((String) request.get("description"));
        sc.setCoverImageUrl((String) request.get("coverImageUrl"));
        sc.setIsPublic(request.get("isPublic") != null ? (Boolean) request.get("isPublic") : true);
        sc = showcaseRepository.save(sc);
        return toMap(sc, false);
    }

    @Transactional
    public Map<String, Object> addItem(Long userId, Long showcaseId, Map<String, Object> request) {
        Showcase sc = showcaseRepository.findById(showcaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Showcase", "id", showcaseId));
        if (!sc.getUser().getId().equals(userId)) throw new BadRequestException("Không có quyền");

        ShowcaseItem item = new ShowcaseItem();
        item.setShowcase(sc);
        item.setName((String) request.get("name"));
        item.setBrand((String) request.get("brand"));
        item.setScale((String) request.get("scale"));
        item.setImageUrl((String) request.get("imageUrl"));
        item.setDescription((String) request.get("description"));
        showcaseItemRepository.save(item);

        sc.setItemCount(sc.getItemCount() + 1);
        showcaseRepository.save(sc);

        Map<String, Object> m = new HashMap<>();
        m.put("id", item.getId());
        m.put("name", item.getName());
        m.put("imageUrl", item.getImageUrl());
        return m;
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
}