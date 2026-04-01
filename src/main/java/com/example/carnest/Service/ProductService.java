package com.example.carnest.Service;

import com.example.carnest.Entity.*;
import com.example.carnest.Enum.ProductCondition;
import com.example.carnest.Enum.ProductStatus;
import com.example.carnest.Exception.BadRequestException;
import com.example.carnest.Exception.ResourceNotFoundException;
import com.example.carnest.Model.ProductDTO;
import com.example.carnest.Model.ShopDTO;
import com.example.carnest.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ShopRepository shopRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;

    private static final int MAX_SIZE = 50;
    private static final int MAX_IMAGES = 10;

    @Autowired
    public ProductService(ProductRepository productRepository,
                          ProductImageRepository productImageRepository,
                          ShopRepository shopRepository,
                          CategoryRepository categoryRepository,
                          BrandRepository brandRepository) {
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.shopRepository = shopRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
    }

    // ===== TẠO SẢN PHẨM =====
    @Transactional
    public ProductDTO.ProductResponse create(Long userId, ProductDTO.CreateRequest request) {
        Shop shop = shopRepository.findByUserIdWithUser(userId)
                .orElseThrow(() -> new BadRequestException("Bạn chưa tạo shop. Hãy tạo shop trước khi đăng bán."));

        Product product = new Product();
        product.setShop(shop);
        product.setName(request.getName().trim());
        product.setSlug(generateSlug(request.getName()));
        product.setDescription(request.getDescription());
        product.setScale(request.getScale());
        product.setCarBrand(request.getCarBrand());
        product.setCarModel(request.getCarModel());
        product.setYearMade(request.getYearMade());
        product.setColor(request.getColor());
        product.setMaterial(request.getMaterial());
        product.setCondition(request.getCondition());
        product.setConditionNote(request.getConditionNote());
        product.setPrice(request.getPrice());
        product.setOriginalPrice(request.getOriginalPrice());
        product.setQuantity(request.getQuantity() != null ? request.getQuantity() : 1);
        product.setWeightGram(request.getWeightGram());
        product.setFreeShipping(request.getFreeShipping() != null ? request.getFreeShipping() : false);
        product.setIsCombo(request.getIsCombo() != null ? request.getIsCombo() : false);
        product.setComboQuantity(request.getComboQuantity() != null ? request.getComboQuantity() : 1);
        product.setAllowOffer(request.getAllowOffer() != null ? request.getAllowOffer() : true);
        product.setMinOfferPrice(request.getMinOfferPrice());
        product.setBulkDiscountMin(request.getBulkDiscountMin());
        product.setBulkDiscountPct(request.getBulkDiscountPct());
        product.setMetaTitle(request.getMetaTitle());
        product.setMetaDescription(request.getMetaDescription());
        product.setStatus(ProductStatus.ACTIVE);
        product.setViewCount(0);
        product.setFavoriteCount(0);
        product.setSoldCount(0);
        product.setIsVerified(false);

        if (request.getCategoryId() != null) {
            Category cat = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
            product.setCategory(cat);
        }
        if (request.getBrandId() != null) {
            Brand brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", request.getBrandId()));
            product.setBrand(brand);
        }

        product = productRepository.save(product);

        // Lưu ảnh
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            int limit = Math.min(request.getImageUrls().size(), MAX_IMAGES);
            for (int i = 0; i < limit; i++) {
                ProductImage img = new ProductImage();
                img.setProduct(product);
                img.setImageUrl(request.getImageUrls().get(i));
                img.setSortOrder(i);
                img.setIsPrimary(i == 0);
                productImageRepository.save(img);
            }
        }

        // Cập nhật totalProducts của shop
        shop.setTotalProducts(shop.getTotalProducts() + 1);
        shopRepository.save(shop);

        return getById(product.getId());
    }

    // ===== CẬP NHẬT SẢN PHẨM =====
    @Transactional
    public ProductDTO.ProductResponse update(Long userId, Long productId, ProductDTO.CreateRequest request) {
        Product product = productRepository.findByIdFull(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        // Kiểm tra chủ sở hữu
        if (!product.getShop().getUser().getId().equals(userId)) {
            throw new BadRequestException("Bạn không có quyền sửa sản phẩm này");
        }

        if (request.getName() != null) {
            product.setName(request.getName().trim());
            product.setSlug(generateSlug(request.getName()));
        }
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getScale() != null) product.setScale(request.getScale());
        if (request.getCarBrand() != null) product.setCarBrand(request.getCarBrand());
        if (request.getCarModel() != null) product.setCarModel(request.getCarModel());
        if (request.getYearMade() != null) product.setYearMade(request.getYearMade());
        if (request.getColor() != null) product.setColor(request.getColor());
        if (request.getMaterial() != null) product.setMaterial(request.getMaterial());
        if (request.getCondition() != null) product.setCondition(request.getCondition());
        if (request.getConditionNote() != null) product.setConditionNote(request.getConditionNote());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getOriginalPrice() != null) product.setOriginalPrice(request.getOriginalPrice());
        if (request.getQuantity() != null) product.setQuantity(request.getQuantity());
        if (request.getWeightGram() != null) product.setWeightGram(request.getWeightGram());
        if (request.getFreeShipping() != null) product.setFreeShipping(request.getFreeShipping());
        if (request.getIsCombo() != null) product.setIsCombo(request.getIsCombo());
        if (request.getComboQuantity() != null) product.setComboQuantity(request.getComboQuantity());
        if (request.getAllowOffer() != null) product.setAllowOffer(request.getAllowOffer());
        if (request.getMinOfferPrice() != null) product.setMinOfferPrice(request.getMinOfferPrice());
        if (request.getMetaTitle() != null) product.setMetaTitle(request.getMetaTitle());
        if (request.getMetaDescription() != null) product.setMetaDescription(request.getMetaDescription());

        if (request.getCategoryId() != null) {
            Category cat = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
            product.setCategory(cat);
        }
        if (request.getBrandId() != null) {
            Brand brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", request.getBrandId()));
            product.setBrand(brand);
        }

        // Cập nhật ảnh (xóa cũ, thêm mới)
        if (request.getImageUrls() != null) {
            productImageRepository.deleteByProductId(productId);
            int limit = Math.min(request.getImageUrls().size(), MAX_IMAGES);
            for (int i = 0; i < limit; i++) {
                ProductImage img = new ProductImage();
                img.setProduct(product);
                img.setImageUrl(request.getImageUrls().get(i));
                img.setSortOrder(i);
                img.setIsPrimary(i == 0);
                productImageRepository.save(img);
            }
        }

        productRepository.save(product);
        return getById(productId);
    }

    // ===== XÓA SẢN PHẨM (soft delete) =====
    @Transactional
    public void delete(Long userId, Long productId) {
        Product product = productRepository.findByIdFull(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        if (!product.getShop().getUser().getId().equals(userId)) {
            throw new BadRequestException("Bạn không có quyền xóa sản phẩm này");
        }
        product.setStatus(ProductStatus.DELETED);
        productRepository.save(product);

        Shop shop = product.getShop();
        shop.setTotalProducts(Math.max(0, shop.getTotalProducts() - 1));
        shopRepository.save(shop);
    }

    // ===== XEM CHI TIẾT SẢN PHẨM =====
    @Transactional
    public ProductDTO.ProductResponse getById(Long id) {
        Product p = productRepository.findByIdFull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        if (p.getStatus() == ProductStatus.DELETED) {
            throw new ResourceNotFoundException("Product", "id", id);
        }

        // Tăng view count
        p.setViewCount(p.getViewCount() + 1);
        productRepository.save(p);

        List<ProductImage> images = productImageRepository.findByProductIdOrderBySortOrderAsc(id);
        return toProductResponse(p, images);
    }

    public ProductDTO.ProductResponse getBySlug(String slug) {
        Product p = productRepository.findBySlugFull(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "slug", slug));
        if (p.getStatus() == ProductStatus.DELETED) {
            throw new ResourceNotFoundException("Product", "slug", slug);
        }
        p.setViewCount(p.getViewCount() + 1);
        productRepository.save(p);
        List<ProductImage> images = productImageRepository.findByProductIdOrderBySortOrderAsc(p.getId());
        return toProductResponse(p, images);
    }

    // ===== DANH SÁCH SẢN PHẨM — cursor-based + filter =====
    public ShopDTO.CursorPage<ProductDTO.ProductSummary> getProducts(
            String sortBy, String cursor, int size,
            Long shopId, Long categoryId, Long brandId,
            String scale, ProductCondition condition,
            BigDecimal minPrice, BigDecimal maxPrice, String keyword) {

        size = Math.min(Math.max(size, 1), MAX_SIZE);
        int fetchSize = size + 1;

        List<Product> products;
        Long cursorId = null;

        // Nếu có filter → dùng filterProducts
        boolean hasFilter = shopId != null || categoryId != null || brandId != null
                || scale != null || condition != null || minPrice != null || maxPrice != null || keyword != null;

        if (hasFilter) {
            if (cursor != null && !cursor.isEmpty()) cursorId = Long.parseLong(cursor);
            products = productRepository.filterProducts(
                    shopId, categoryId, brandId, scale, condition, minPrice, maxPrice, keyword, cursorId, fetchSize);
        } else if ("price_asc".equals(sortBy)) {
            if (cursor == null || cursor.isEmpty()) {
                products = productRepository.findByPriceAsc(fetchSize);
            } else {
                String[] parts = cursor.split("_");
                products = productRepository.findByPriceAscAfterCursor(
                        new BigDecimal(parts[0]), Long.parseLong(parts[1]), fetchSize);
            }
        } else if ("price_desc".equals(sortBy)) {
            if (cursor == null || cursor.isEmpty()) {
                products = productRepository.findByPriceDesc(fetchSize);
            } else {
                String[] parts = cursor.split("_");
                products = productRepository.findByPriceDescAfterCursor(
                        new BigDecimal(parts[0]), Long.parseLong(parts[1]), fetchSize);
            }
        } else {
            // Default: newest
            if (cursor == null || cursor.isEmpty()) {
                products = productRepository.findNewest(fetchSize);
            } else {
                products = productRepository.findNewestAfterCursor(Long.parseLong(cursor), fetchSize);
            }
        }

        boolean hasMore = products.size() > size;
        if (hasMore) products = products.subList(0, size);

        // Batch fetch primary images — 1 query cho tất cả
        Map<Long, String> primaryImageMap = new HashMap<>();
        if (!products.isEmpty()) {
            List<Long> productIds = products.stream().map(Product::getId).collect(Collectors.toList());
            List<ProductImage> primaryImages = productImageRepository.findPrimaryByProductIds(productIds);
            primaryImages.forEach(img -> primaryImageMap.put(img.getProduct().getId(), img.getImageUrl()));
        }

        List<ProductDTO.ProductSummary> items = products.stream()
                .map(p -> toProductSummary(p, primaryImageMap.get(p.getId())))
                .collect(Collectors.toList());

        // Build cursor
        String nextCursor = null;
        if (hasMore && !products.isEmpty()) {
            Product last = products.get(products.size() - 1);
            if ("price_asc".equals(sortBy) || "price_desc".equals(sortBy)) {
                nextCursor = last.getPrice().toPlainString() + "_" + last.getId();
            } else {
                nextCursor = String.valueOf(last.getId());
            }
        }

        Long total = hasFilter
                ? productRepository.countFiltered(shopId, categoryId, brandId, scale, condition, minPrice, maxPrice, keyword)
                : productRepository.countActive();

        return new ShopDTO.CursorPage<>(items, nextCursor, hasMore, items.size(), total);
    }

    // ===== SẢN PHẨM THEO SHOP =====
    public ShopDTO.CursorPage<ProductDTO.ProductSummary> getByShop(Long shopId, String cursor, int size) {
        size = Math.min(Math.max(size, 1), MAX_SIZE);
        int fetchSize = size + 1;

        Long cursorId = (cursor != null && !cursor.isEmpty()) ? Long.parseLong(cursor) : null;
        List<Product> products = productRepository.findByShopId(shopId, cursorId, fetchSize);

        boolean hasMore = products.size() > size;
        if (hasMore) products = products.subList(0, size);

        Map<Long, String> primaryImageMap = new HashMap<>();
        if (!products.isEmpty()) {
            List<Long> ids = products.stream().map(Product::getId).collect(Collectors.toList());
            productImageRepository.findPrimaryByProductIds(ids)
                    .forEach(img -> primaryImageMap.put(img.getProduct().getId(), img.getImageUrl()));
        }

        List<ProductDTO.ProductSummary> items = products.stream()
                .map(p -> toProductSummary(p, primaryImageMap.get(p.getId())))
                .collect(Collectors.toList());

        String nextCursor = null;
        if (hasMore && !products.isEmpty()) {
            nextCursor = String.valueOf(products.get(products.size() - 1).getId());
        }

        Long total = productRepository.countByShopId(shopId);
        return new ShopDTO.CursorPage<>(items, nextCursor, hasMore, items.size(), total);
    }

    // ===== HELPERS =====
    private ProductDTO.ProductResponse toProductResponse(Product p, List<ProductImage> images) {
        ProductDTO.ProductResponse r = new ProductDTO.ProductResponse();
        r.setId(p.getId());
        r.setName(p.getName());
        r.setSlug(p.getSlug());
        r.setDescription(p.getDescription());
        r.setScale(p.getScale());
        r.setCarBrand(p.getCarBrand());
        r.setCarModel(p.getCarModel());
        r.setYearMade(p.getYearMade());
        r.setColor(p.getColor());
        r.setMaterial(p.getMaterial());
        r.setCondition(p.getCondition().name());
        r.setConditionNote(p.getConditionNote());
        r.setPrice(p.getPrice());
        r.setOriginalPrice(p.getOriginalPrice());
        r.setQuantity(p.getQuantity());
        r.setWeightGram(p.getWeightGram());
        r.setFreeShipping(p.getFreeShipping());
        r.setIsCombo(p.getIsCombo());
        r.setComboQuantity(p.getComboQuantity());
        r.setIsVerified(p.getIsVerified());
        r.setAllowOffer(p.getAllowOffer());
        r.setMinOfferPrice(p.getMinOfferPrice());
        r.setBulkDiscountMin(p.getBulkDiscountMin());
        r.setBulkDiscountPct(p.getBulkDiscountPct());
        r.setViewCount(p.getViewCount());
        r.setFavoriteCount(p.getFavoriteCount());
        r.setSoldCount(p.getSoldCount());
        r.setStatus(p.getStatus().name());
        r.setCreatedAt(p.getCreatedAt());

        // Images
        r.setImages(images.stream().map(img -> {
            ProductDTO.ImageInfo i = new ProductDTO.ImageInfo();
            i.setId(img.getId()); i.setImageUrl(img.getImageUrl());
            i.setThumbnailUrl(img.getThumbnailUrl()); i.setIsPrimary(img.getIsPrimary());
            return i;
        }).collect(Collectors.toList()));

        // Shop (already fetched)
        Shop shop = p.getShop();
        ProductDTO.ShopInfo si = new ProductDTO.ShopInfo();
        si.setId(shop.getId()); si.setShopName(shop.getShopName());
        si.setSlug(shop.getSlug()); si.setLogoUrl(shop.getLogoUrl());
        r.setShop(si);

        // Category
        if (p.getCategory() != null) {
            ProductDTO.CategoryInfo ci = new ProductDTO.CategoryInfo();
            ci.setId(p.getCategory().getId()); ci.setName(p.getCategory().getName());
            ci.setSlug(p.getCategory().getSlug());
            r.setCategory(ci);
        }

        // Brand
        if (p.getBrand() != null) {
            ProductDTO.BrandInfo bi = new ProductDTO.BrandInfo();
            bi.setId(p.getBrand().getId()); bi.setName(p.getBrand().getName());
            bi.setSlug(p.getBrand().getSlug());
            r.setBrand(bi);
        }

        return r;
    }

    private ProductDTO.ProductSummary toProductSummary(Product p, String primaryImage) {
        ProductDTO.ProductSummary s = new ProductDTO.ProductSummary();
        s.setId(p.getId());
        s.setName(p.getName());
        s.setSlug(p.getSlug());
        s.setPrimaryImage(primaryImage);
        s.setPrice(p.getPrice());
        s.setOriginalPrice(p.getOriginalPrice());
        s.setScale(p.getScale());
        s.setCondition(p.getCondition().name());
        s.setIsVerified(p.getIsVerified());
        s.setFreeShipping(p.getFreeShipping());
        s.setCreatedAt(p.getCreatedAt());

        // Shop info (already JOIN FETCH)
        s.setShopName(p.getShop().getShopName());
        s.setShopSlug(p.getShop().getSlug());

        // Brand info
        if (p.getBrand() != null) {
            s.setBrandName(p.getBrand().getName());
        }

        return s;
    }

    private String generateSlug(String name) {
        String slug = name.toLowerCase().trim()
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
        String base = slug; int c = 1;
        while (productRepository.existsBySlug(slug)) { slug = base + "-" + c++; }
        return slug;
    }
}
