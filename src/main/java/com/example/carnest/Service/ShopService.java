package com.example.carnest.Service;

import com.example.carnest.Entity.Shop;
import com.example.carnest.Entity.ShopFollower;
import com.example.carnest.Entity.User;
import com.example.carnest.Exception.BadRequestException;
import com.example.carnest.Exception.ResourceNotFoundException;
import com.example.carnest.Model.ShopDTO;
import com.example.carnest.Repository.ShopFollowerRepository;
import com.example.carnest.Repository.ShopRepository;
import com.example.carnest.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ShopService {

    private final ShopRepository shopRepository;
    private final ShopFollowerRepository shopFollowerRepository;
    private final UserRepository userRepository;

    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 50;

    @Autowired
    public ShopService(ShopRepository shopRepository,
                       ShopFollowerRepository shopFollowerRepository,
                       UserRepository userRepository) {
        this.shopRepository = shopRepository;
        this.shopFollowerRepository = shopFollowerRepository;
        this.userRepository = userRepository;
    }

    // ===== TẠO SHOP =====
    @Transactional
    public ShopDTO.ShopResponse createShop(Long userId, ShopDTO.CreateShopRequest request) {
        if (shopRepository.existsByUserId(userId)) {
            throw new BadRequestException("Bạn đã có shop rồi, mỗi tài khoản chỉ được tạo 1 shop");
        }
        if (shopRepository.existsByShopName(request.getShopName().trim())) {
            throw new BadRequestException("Tên shop đã được sử dụng");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        String slug = generateSlug(request.getShopName());

        Shop shop = new Shop();
        shop.setUser(user);
        shop.setShopName(request.getShopName().trim());
        shop.setSlug(slug);
        shop.setDescription(request.getDescription());
        shop.setLogoUrl(request.getLogoUrl());
        shop.setBannerUrl(request.getBannerUrl());
        shop.setReturnPolicy(request.getReturnPolicy());
        shop.setShippingInfo(request.getShippingInfo());
        shop.setIsActive(true);
        shop.setIsVerified(false);
        shop.setTotalProducts(0);
        shop.setTotalSold(0);
        shop.setFollowerCount(0);

        shop = shopRepository.save(shop);

        user.setIsSeller(true);
        userRepository.save(user);

        return toShopResponse(shop, false);
    }

    // ===== CẬP NHẬT SHOP =====
    @Transactional
    public ShopDTO.ShopResponse updateShop(Long userId, ShopDTO.UpdateShopRequest request) {
        Shop shop = shopRepository.findByUserIdWithUser(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop", "userId", userId));

        if (request.getShopName() != null && !request.getShopName().trim().equals(shop.getShopName())) {
            if (shopRepository.existsByShopName(request.getShopName().trim())) {
                throw new BadRequestException("Tên shop đã được sử dụng");
            }
            shop.setShopName(request.getShopName().trim());
            shop.setSlug(generateSlug(request.getShopName()));
        }
        if (request.getDescription() != null) shop.setDescription(request.getDescription());
        if (request.getLogoUrl() != null) shop.setLogoUrl(request.getLogoUrl());
        if (request.getBannerUrl() != null) shop.setBannerUrl(request.getBannerUrl());
        if (request.getReturnPolicy() != null) shop.setReturnPolicy(request.getReturnPolicy());
        if (request.getShippingInfo() != null) shop.setShippingInfo(request.getShippingInfo());

        shop = shopRepository.save(shop);
        return toShopResponse(shop, false);
    }

    // ===== XEM SHOP CỦA TÔI =====
    public ShopDTO.ShopResponse getMyShop(Long userId) {
        Shop shop = shopRepository.findByUserIdWithUser(userId)
                .orElseThrow(() -> new BadRequestException("Bạn chưa tạo shop"));
        return toShopResponse(shop, false);
    }

    // ===== XEM SHOP THEO SLUG (public) =====
    public ShopDTO.ShopResponse getShopBySlug(String slug, Long currentUserId) {
        Shop shop = shopRepository.findBySlugWithUser(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Shop", "slug", slug));
        if (!shop.getIsActive()) {
            throw new ResourceNotFoundException("Shop", "slug", slug);
        }

        boolean isFollowing = false;
        if (currentUserId != null) {
            isFollowing = shopFollowerRepository.existsByShopIdAndUserId(shop.getId(), currentUserId);
        }
        return toShopResponse(shop, isFollowing);
    }

    // ===== XEM SHOP THEO ID (public) =====
    public ShopDTO.ShopResponse getShopById(Long shopId, Long currentUserId) {
        Shop shop = shopRepository.findByIdWithUser(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop", "id", shopId));
        if (!shop.getIsActive()) {
            throw new ResourceNotFoundException("Shop", "id", shopId);
        }

        boolean isFollowing = false;
        if (currentUserId != null) {
            isFollowing = shopFollowerRepository.existsByShopIdAndUserId(shop.getId(), currentUserId);
        }
        return toShopResponse(shop, isFollowing);
    }

    // ===== DANH SÁCH SHOP — cursor-based =====
    public ShopDTO.CursorPage<ShopDTO.ShopSummary> getShops(
            String sortBy, String cursor, int size, Long currentUserId) {

        size = Math.min(Math.max(size, 1), MAX_SIZE);
        int fetchSize = size + 1; // Lấy thêm 1 để check hasMore

        List<Shop> shops;

        if ("rating".equals(sortBy)) {
            shops = fetchShopsByRating(cursor, fetchSize);
        } else if ("newest".equals(sortBy)) {
            shops = fetchShopsByNewest(cursor, fetchSize);
        } else {
            // default: followerCount
            shops = fetchShopsByFollower(cursor, fetchSize);
        }

        boolean hasMore = shops.size() > size;
        if (hasMore) {
            shops = shops.subList(0, size); // Bỏ record thừa
        }

        // Batch check follow status — 1 query thay vì N query
        Set<Long> followedIds = Collections.emptySet();
        if (currentUserId != null && !shops.isEmpty()) {
            List<Long> shopIds = shops.stream().map(Shop::getId).collect(Collectors.toList());
            followedIds = shopFollowerRepository.findFollowedShopIds(currentUserId, shopIds);
        }

        // Convert to DTO
        Set<Long> finalFollowedIds = followedIds;
        List<ShopDTO.ShopSummary> items = shops.stream()
                .map(shop -> toShopSummary(shop, finalFollowedIds.contains(shop.getId())))
                .collect(Collectors.toList());

        // Build next cursor
        String nextCursor = null;
        if (hasMore && !shops.isEmpty()) {
            Shop last = shops.get(shops.size() - 1);
            if ("rating".equals(sortBy)) {
                nextCursor = last.getRatingAvg().toPlainString() + "_" + last.getId();
            } else if ("newest".equals(sortBy)) {
                nextCursor = String.valueOf(last.getId());
            } else {
                nextCursor = last.getFollowerCount() + "_" + last.getId();
            }
        }

        Long total = shopRepository.countActiveShops();
        return new ShopDTO.CursorPage<>(items, nextCursor, hasMore, items.size(), total);
    }

    // ===== TÌM KIẾM SHOP — cursor-based =====
    public ShopDTO.CursorPage<ShopDTO.ShopSummary> searchShops(
            String keyword, String cursor, int size, Long currentUserId) {

        size = Math.min(Math.max(size, 1), MAX_SIZE);
        int fetchSize = size + 1;

        List<Shop> shops;
        if (cursor == null || cursor.isEmpty()) {
            shops = shopRepository.searchByName(keyword, fetchSize);
        } else {
            String[] parts = cursor.split("_");
            Integer cursorFollower = Integer.parseInt(parts[0]);
            Long cursorId = Long.parseLong(parts[1]);
            shops = shopRepository.searchByNameAfterCursor(keyword, cursorFollower, cursorId, fetchSize);
        }

        boolean hasMore = shops.size() > size;
        if (hasMore) {
            shops = shops.subList(0, size);
        }

        Set<Long> followedIds = Collections.emptySet();
        if (currentUserId != null && !shops.isEmpty()) {
            List<Long> shopIds = shops.stream().map(Shop::getId).collect(Collectors.toList());
            followedIds = shopFollowerRepository.findFollowedShopIds(currentUserId, shopIds);
        }

        Set<Long> finalFollowedIds = followedIds;
        List<ShopDTO.ShopSummary> items = shops.stream()
                .map(shop -> toShopSummary(shop, finalFollowedIds.contains(shop.getId())))
                .collect(Collectors.toList());

        String nextCursor = null;
        if (hasMore && !shops.isEmpty()) {
            Shop last = shops.get(shops.size() - 1);
            nextCursor = last.getFollowerCount() + "_" + last.getId();
        }

        Long total = shopRepository.countByKeyword(keyword);
        return new ShopDTO.CursorPage<>(items, nextCursor, hasMore, items.size(), total);
    }

    // ===== FOLLOW SHOP =====
    @Transactional
    public String followShop(Long userId, Long shopId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop", "id", shopId));

        if (shop.getUser().getId().equals(userId)) {
            throw new BadRequestException("Bạn không thể follow shop của chính mình");
        }
        if (shopFollowerRepository.existsByShopIdAndUserId(shopId, userId)) {
            throw new BadRequestException("Bạn đã follow shop này rồi");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        ShopFollower follower = new ShopFollower();
        follower.setShop(shop);
        follower.setUser(user);
        shopFollowerRepository.save(follower);

        shop.setFollowerCount(shop.getFollowerCount() + 1);
        shopRepository.save(shop);

        return "Follow shop thành công";
    }

    // ===== UNFOLLOW SHOP =====
    @Transactional
    public String unfollowShop(Long userId, Long shopId) {
        ShopFollower follower = shopFollowerRepository.findByShopIdAndUserId(shopId, userId)
                .orElseThrow(() -> new BadRequestException("Bạn chưa follow shop này"));

        shopFollowerRepository.delete(follower);

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop", "id", shopId));
        shop.setFollowerCount(Math.max(0, shop.getFollowerCount() - 1));
        shopRepository.save(shop);

        return "Unfollow shop thành công";
    }

    // ===== DANH SÁCH SHOP ĐANG FOLLOW — cursor-based =====
    public ShopDTO.CursorPage<ShopDTO.ShopSummary> getFollowingShops(
            Long userId, String cursor, int size) {

        size = Math.min(Math.max(size, 1), MAX_SIZE);
        int fetchSize = size + 1;

        List<ShopFollower> followers;
        if (cursor == null || cursor.isEmpty()) {
            followers = shopFollowerRepository.findByUserIdWithShop(userId, fetchSize);
        } else {
            Long cursorId = Long.parseLong(cursor);
            followers = shopFollowerRepository.findByUserIdAfterCursor(userId, cursorId, fetchSize);
        }

        boolean hasMore = followers.size() > size;
        if (hasMore) {
            followers = followers.subList(0, size);
        }

        List<ShopDTO.ShopSummary> items = followers.stream()
                .map(f -> toShopSummary(f.getShop(), true)) // đang follow nên isFollowing = true
                .collect(Collectors.toList());

        String nextCursor = null;
        if (hasMore && !followers.isEmpty()) {
            nextCursor = String.valueOf(followers.get(followers.size() - 1).getId());
        }

        Long total = shopFollowerRepository.countByUserId(userId);
        return new ShopDTO.CursorPage<>(items, nextCursor, hasMore, items.size(), total);
    }

    // ===== PRIVATE: fetch shops by sort type =====
    private List<Shop> fetchShopsByFollower(String cursor, int fetchSize) {
        if (cursor == null || cursor.isEmpty()) {
            return shopRepository.findTopShops(fetchSize);
        }
        String[] parts = cursor.split("_");
        Integer cursorFollower = Integer.parseInt(parts[0]);
        Long cursorId = Long.parseLong(parts[1]);
        return shopRepository.findShopsAfterCursor(cursorFollower, cursorId, fetchSize);
    }

    private List<Shop> fetchShopsByRating(String cursor, int fetchSize) {
        if (cursor == null || cursor.isEmpty()) {
            return shopRepository.findTopShopsByRating(fetchSize);
        }
        String[] parts = cursor.split("_");
        BigDecimal cursorRating = new BigDecimal(parts[0]);
        Long cursorId = Long.parseLong(parts[1]);
        return shopRepository.findShopsByRatingAfterCursor(cursorRating, cursorId, fetchSize);
    }

    private List<Shop> fetchShopsByNewest(String cursor, int fetchSize) {
        if (cursor == null || cursor.isEmpty()) {
            return shopRepository.findNewestShops(fetchSize);
        }
        Long cursorId = Long.parseLong(cursor);
        return shopRepository.findNewestShopsAfterCursor(cursorId, fetchSize);
    }

    // ===== HELPER: tạo slug =====
    private String generateSlug(String shopName) {
        String slug = shopName.toLowerCase().trim()
                .replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a")
                .replaceAll("[èéẹẻẽêềếệểễ]", "e")
                .replaceAll("[ìíịỉĩ]", "i")
                .replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o")
                .replaceAll("[ùúụủũưừứựửữ]", "u")
                .replaceAll("[ỳýỵỷỹ]", "y")
                .replaceAll("[đ]", "d")
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("[\\s]+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");

        String baseSlug = slug;
        int counter = 1;
        while (shopRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter;
            counter++;
        }
        return slug;
    }

    // ===== HELPER: convert Entity -> ShopResponse =====
    private ShopDTO.ShopResponse toShopResponse(Shop shop, boolean isFollowing) {
        ShopDTO.ShopResponse response = new ShopDTO.ShopResponse();
        response.setId(shop.getId());
        response.setShopName(shop.getShopName());
        response.setSlug(shop.getSlug());
        response.setDescription(shop.getDescription());
        response.setLogoUrl(shop.getLogoUrl());
        response.setBannerUrl(shop.getBannerUrl());
        response.setReturnPolicy(shop.getReturnPolicy());
        response.setShippingInfo(shop.getShippingInfo());
        response.setTotalProducts(shop.getTotalProducts());
        response.setTotalSold(shop.getTotalSold());
        response.setFollowerCount(shop.getFollowerCount());
        response.setRatingAvg(shop.getRatingAvg());
        response.setIsVerified(shop.getIsVerified());
        response.setIsFollowing(isFollowing);
        response.setCreatedAt(shop.getCreatedAt());

        // User đã được JOIN FETCH — không gây thêm query
        User owner = shop.getUser();
        ShopDTO.OwnerInfo ownerInfo = new ShopDTO.OwnerInfo();
        ownerInfo.setId(owner.getId());
        ownerInfo.setUsername(owner.getUsername());
        ownerInfo.setFullName(owner.getFullName());
        ownerInfo.setAvatarUrl(owner.getAvatarUrl());
        ownerInfo.setSellerRatingAvg(owner.getSellerRatingAvg());
        ownerInfo.setTotalSold(owner.getTotalSold());
        response.setOwner(ownerInfo);

        return response;
    }

    // ===== HELPER: convert Entity -> ShopSummary =====
    private ShopDTO.ShopSummary toShopSummary(Shop shop, boolean isFollowing) {
        ShopDTO.ShopSummary summary = new ShopDTO.ShopSummary();
        summary.setId(shop.getId());
        summary.setShopName(shop.getShopName());
        summary.setSlug(shop.getSlug());
        summary.setLogoUrl(shop.getLogoUrl());
        summary.setTotalProducts(shop.getTotalProducts());
        summary.setFollowerCount(shop.getFollowerCount());
        summary.setRatingAvg(shop.getRatingAvg());
        summary.setIsVerified(shop.getIsVerified());
        summary.setIsFollowing(isFollowing);
        return summary;
    }
}
