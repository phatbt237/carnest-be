package com.example.carnest.Service;

import com.example.carnest.Entity.*;
import com.example.carnest.Enum.ProductStatus;
import com.example.carnest.Exception.BadRequestException;
import com.example.carnest.Exception.ResourceNotFoundException;
import com.example.carnest.Model.CartDTO;
import com.example.carnest.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final UserRepository userRepository;

    @Autowired
    public CartService(CartItemRepository cartItemRepository,
                       ProductRepository productRepository,
                       ProductImageRepository productImageRepository,
                       UserRepository userRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.userRepository = userRepository;
    }

    // Thêm vào giỏ
    @Transactional
    public String addToCart(Long userId, CartDTO.AddToCartRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

        if (product.getStatus() != ProductStatus.ACTIVE) {
            throw new BadRequestException("Sản phẩm không còn bán");
        }
        // Không mua sản phẩm của chính mình
        if (product.getShop().getUser().getId().equals(userId)) {
            throw new BadRequestException("Bạn không thể mua sản phẩm của chính mình");
        }

        Optional<CartItem> existing = cartItemRepository.findByUserIdAndProductId(userId, request.getProductId());
        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            cartItemRepository.save(item);
        } else {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
            CartItem item = new CartItem();
            item.setUser(user);
            item.setProduct(product);
            item.setQuantity(request.getQuantity());
            cartItemRepository.save(item);
        }
        return "Đã thêm vào giỏ hàng";
    }

    // Xóa khỏi giỏ
    @Transactional
    public String removeFromCart(Long userId, Long productId) {
        if (!cartItemRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new BadRequestException("Sản phẩm không có trong giỏ hàng");
        }
        cartItemRepository.deleteByUserIdAndProductId(userId, productId);
        return "Đã xóa khỏi giỏ hàng";
    }

    // Xóa toàn bộ giỏ
    @Transactional
    public String clearCart(Long userId) {
        cartItemRepository.deleteByUserId(userId);
        return "Đã xóa toàn bộ giỏ hàng";
    }

    // Xem giỏ hàng
    public CartDTO.CartResponse getCart(Long userId) {
        List<CartItem> items = cartItemRepository.findByUserIdWithProduct(userId);

        // Batch fetch primary images
        Map<Long, String> imageMap = new HashMap<>();
        if (!items.isEmpty()) {
            List<Long> productIds = items.stream()
                    .map(ci -> ci.getProduct().getId()).collect(Collectors.toList());
            productImageRepository.findPrimaryByProductIds(productIds)
                    .forEach(img -> imageMap.put(img.getProduct().getId(), img.getImageUrl()));
        }

        BigDecimal totalPrice = BigDecimal.ZERO;
        Set<Long> shopIds = new HashSet<>();
        List<CartDTO.CartItemInfo> itemInfos = new ArrayList<>();

        for (CartItem ci : items) {
            Product p = ci.getProduct();
            CartDTO.CartItemInfo info = new CartDTO.CartItemInfo();
            info.setId(ci.getId());
            info.setProductId(p.getId());
            info.setProductName(p.getName());
            info.setProductImage(imageMap.get(p.getId()));
            info.setPrice(p.getPrice());
            info.setQuantity(ci.getQuantity());
            info.setScale(p.getScale());
            info.setCondition(p.getCondition().name());
            info.setShopId(p.getShop().getId());
            info.setShopName(p.getShop().getShopName());
            info.setShopSlug(p.getShop().getSlug());
            if (p.getBrand() != null) info.setBrandName(p.getBrand().getName());
            info.setIsAvailable(p.getStatus() == ProductStatus.ACTIVE);

            itemInfos.add(info);
            totalPrice = totalPrice.add(p.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
            shopIds.add(p.getShop().getId());
        }

        CartDTO.CartResponse response = new CartDTO.CartResponse();
        response.setItems(itemInfos);
        response.setTotalItems(itemInfos.size());
        response.setTotalPrice(totalPrice);
        response.setShopCount(shopIds.size());
        return response;
    }
}
