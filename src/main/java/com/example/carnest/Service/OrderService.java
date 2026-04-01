package com.example.carnest.Service;

import com.example.carnest.Entity.*;
import com.example.carnest.Enum.*;
import com.example.carnest.Exception.BadRequestException;
import com.example.carnest.Exception.ResourceNotFoundException;
import com.example.carnest.Model.OrderDTO;
import com.example.carnest.Model.ShopDTO;
import com.example.carnest.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderStatusHistoryRepository statusHistoryRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;

    private static final int AUTO_COMPLETE_DAYS = 7;
    private static final int MAX_SIZE = 50;

    @Autowired
    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
                        OrderStatusHistoryRepository statusHistoryRepository,
                        CartItemRepository cartItemRepository, ProductRepository productRepository,
                        ProductImageRepository productImageRepository, ShopRepository shopRepository,
                        UserRepository userRepository, WalletRepository walletRepository,
                        WalletTransactionRepository walletTransactionRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.shopRepository = shopRepository;
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.walletTransactionRepository = walletTransactionRepository;
    }

    // ===== CHECKOUT — tạo đơn hàng từ giỏ =====
//    @Transactional
//    public List<OrderDTO.OrderResponse> checkout(Long userId, OrderDTO.CheckoutRequest request) {
//        List<CartItem> cartItems = cartItemRepository.findByUserIdWithProduct(userId);
//        if (cartItems.isEmpty()) {
//            throw new BadRequestException("Giỏ hàng trống");
//        }
//
//        // Lọc theo productIds nếu có
//        if (request.getProductIds() != null && !request.getProductIds().isEmpty()) {
//            Set<Long> selectedIds = new HashSet<>(request.getProductIds());
//            cartItems = cartItems.stream()
//                    .filter(ci -> selectedIds.contains(ci.getProduct().getId()))
//                    .collect(Collectors.toList());
//            if (cartItems.isEmpty()) {
//                throw new BadRequestException("Không tìm thấy sản phẩm đã chọn trong giỏ hàng");
//            }
//        }
//
//        // Validate: sản phẩm còn bán không
//        for (CartItem ci : cartItems) {
//            if (ci.getProduct().getStatus() != ProductStatus.ACTIVE) {
//                throw new BadRequestException("Sản phẩm '" + ci.getProduct().getName() + "' không còn bán");
//            }
//        }
//
//        // Nhóm theo shop → mỗi shop 1 đơn
//        Map<Long, List<CartItem>> byShop = cartItems.stream()
//                .collect(Collectors.groupingBy(ci -> ci.getProduct().getShop().getId()));
//
//        User buyer = userRepository.findById(userId)
//                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
//
//        // Batch fetch primary images
//        List<Long> productIds = cartItems.stream().map(ci -> ci.getProduct().getId()).collect(Collectors.toList());
//        Map<Long, String> imageMap = new HashMap<>();
//        productImageRepository.findPrimaryByProductIds(productIds)
//                .forEach(img -> imageMap.put(img.getProduct().getId(), img.getImageUrl()));
//
//        List<OrderDTO.OrderResponse> orders = new ArrayList<>();
//
//        for (Map.Entry<Long, List<CartItem>> entry : byShop.entrySet()) {
//            List<CartItem> shopItems = entry.getValue();
//            Shop shop = shopItems.get(0).getProduct().getShop();
//
//            // Tính tổng tiền
//            BigDecimal subtotal = BigDecimal.ZERO;
//            for (CartItem ci : shopItems) {
//                subtotal = subtotal.add(ci.getProduct().getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
//            }
//            BigDecimal totalAmount = subtotal; // Sau này thêm shipping fee, discount
//
//            // Tạo order
//            Order order = new Order();
//            order.setOrderCode(generateOrderCode());
//            order.setBuyer(buyer);
//            order.setShop(shop);
//            order.setShippingName(request.getShippingName());
//            order.setShippingPhone(request.getShippingPhone());
//            order.setShippingAddress(request.getShippingAddress());
//            order.setSubtotal(subtotal);
//            order.setShippingFee(BigDecimal.ZERO);
//            order.setDiscountAmount(BigDecimal.ZERO);
//            order.setTotalAmount(totalAmount);
//            order.setPaymentMethod(request.getPaymentMethod());
//            order.setPaymentStatus(PaymentStatus.PENDING);
//            order.setEscrowStatus(EscrowStatus.NONE);
//            order.setStatus(OrderStatus.PENDING);
//            order.setBuyerNote(request.getBuyerNote());
//            order.setAutoCompleteAt(LocalDateTime.now().plusDays(AUTO_COMPLETE_DAYS));
//
//            order = orderRepository.save(order);
//
//            // Tạo order items
//            for (CartItem ci : shopItems) {
//                Product p = ci.getProduct();
//                OrderItem oi = new OrderItem();
//                oi.setOrder(order);
//                oi.setProduct(p);
//                oi.setProductName(p.getName());
//                oi.setProductImage(imageMap.get(p.getId()));
//                oi.setPrice(p.getPrice());
//                oi.setQuantity(ci.getQuantity());
//                orderItemRepository.save(oi);
//
//                // Đổi trạng thái sản phẩm → RESERVED
//                p.setStatus(ProductStatus.RESERVED);
//                productRepository.save(p);
//            }
//
//            // Ghi lịch sử
//            addStatusHistory(order, null, OrderStatus.PENDING, userId, "Đặt hàng mới");
//
//            orders.add(toOrderResponse(order));
//        }
//
//        // Xóa cart items đã checkout
//        for (CartItem ci : cartItems) {
//            cartItemRepository.delete(ci);
//        }
//
//        return orders;
//    }

    @Transactional
    public List<OrderDTO.OrderResponse> checkout(Long userId, OrderDTO.CheckoutRequest request) {
        List<CartItem> cartItems = cartItemRepository.findByUserIdWithProduct(userId);
        if (cartItems.isEmpty()) {
            throw new BadRequestException("Giỏ hàng trống");
        }

        // Lọc theo productIds nếu có
        if (request.getProductIds() != null && !request.getProductIds().isEmpty()) {
            Set<Long> selectedIds = new HashSet<>(request.getProductIds());
            cartItems = cartItems.stream()
                    .filter(ci -> selectedIds.contains(ci.getProduct().getId()))
                    .collect(Collectors.toList());
            if (cartItems.isEmpty()) {
                throw new BadRequestException("Không tìm thấy sản phẩm đã chọn trong giỏ hàng");
            }
        }

        // Nhóm theo shop
        Map<Long, List<CartItem>> byShop = cartItems.stream()
                .collect(Collectors.groupingBy(ci -> ci.getProduct().getShop().getId()));

        User buyer = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Batch fetch primary images
        List<Long> productIds = cartItems.stream().map(ci -> ci.getProduct().getId()).collect(Collectors.toList());
        Map<Long, String> imageMap = new HashMap<>();
        productImageRepository.findPrimaryByProductIds(productIds)
                .forEach(img -> imageMap.put(img.getProduct().getId(), img.getImageUrl()));

        List<OrderDTO.OrderResponse> orders = new ArrayList<>();

        for (Map.Entry<Long, List<CartItem>> entry : byShop.entrySet()) {
            List<CartItem> shopItems = entry.getValue();
            Shop shop = shopItems.get(0).getProduct().getShop();

            // Lock + kiểm tra + trừ quantity cho từng product
            BigDecimal subtotal = BigDecimal.ZERO;
            List<Product> updatedProducts = new ArrayList<>();

            for (CartItem ci : shopItems) {
                // PESSIMISTIC LOCK — chống race condition
                Product p = productRepository.findByIdForUpdate(ci.getProduct().getId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product", "id", ci.getProduct().getId()));

                if (p.getStatus() != ProductStatus.ACTIVE) {
                    throw new BadRequestException("Sản phẩm '" + p.getName() + "' không còn bán");
                }
                if (p.getQuantity() < ci.getQuantity()) {
                    throw new BadRequestException("Sản phẩm '" + p.getName() + "' chỉ còn " + p.getQuantity() + " chiếc");
                }

                // Trừ quantity
                p.setQuantity(p.getQuantity() - ci.getQuantity());
                if (p.getQuantity() == 0) {
                    p.setStatus(ProductStatus.SOLD);
                }
                updatedProducts.add(p);

                subtotal = subtotal.add(p.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
            }

            // Save tất cả product đã trừ quantity
            for (Product p : updatedProducts) {
                productRepository.save(p);
            }

            BigDecimal totalAmount = subtotal;

            // Tạo order — trạng thái PENDING_PAYMENT, deadline 5 phút
            Order order = new Order();
            order.setOrderCode(generateOrderCode());
            order.setBuyer(buyer);
            order.setShop(shop);
            order.setShippingName(request.getShippingName());
            order.setShippingPhone(request.getShippingPhone());
            order.setShippingAddress(request.getShippingAddress());
            order.setSubtotal(subtotal);
            order.setShippingFee(BigDecimal.ZERO);
            order.setDiscountAmount(BigDecimal.ZERO);
            order.setTotalAmount(totalAmount);
            order.setPaymentMethod(request.getPaymentMethod());
            order.setPaymentStatus(PaymentStatus.PENDING);
            order.setEscrowStatus(EscrowStatus.NONE);
            order.setStatus(OrderStatus.PENDING_PAYMENT);
            order.setPaymentDeadline(LocalDateTime.now().plusMinutes(5));
            order.setBuyerNote(request.getBuyerNote());

            order = orderRepository.save(order);

            // Tạo order items
            for (CartItem ci : shopItems) {
                Product p = ci.getProduct();
                OrderItem oi = new OrderItem();
                oi.setOrder(order);
                oi.setProduct(p);
                oi.setProductName(p.getName());
                oi.setProductImage(imageMap.get(p.getId()));
                oi.setPrice(p.getPrice());
                oi.setQuantity(ci.getQuantity());
                orderItemRepository.save(oi);
            }

            addStatusHistory(order, null, OrderStatus.PENDING_PAYMENT, userId, "Đặt hàng — chờ thanh toán trong 5 phút");
            orders.add(toOrderResponse(order));
        }

        // Xóa cart items đã checkout
        for (CartItem ci : cartItems) {
            cartItemRepository.delete(ci);
        }

        return orders;
    }

    @Transactional
    public OrderDTO.OrderResponse payOrder(Long userId, Long orderId) {
        Order order = getOrderForBuyer(userId, orderId);

        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new BadRequestException("Đơn hàng không ở trạng thái chờ thanh toán");
        }
        if (order.getPaymentDeadline().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Đã hết thời gian thanh toán. Vui lòng đặt hàng lại.");
        }

        // Trừ tiền buyer (pessimistic lock tránh race condition)
        Wallet buyerWallet = walletRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new BadRequestException("Bạn chưa có ví. Vui lòng nạp tiền."));

        if (buyerWallet.getBalance().compareTo(order.getTotalAmount()) < 0) {
            throw new BadRequestException("Số dư không đủ. Cần "
                    + order.getTotalAmount() + " VNĐ, hiện có "
                    + buyerWallet.getBalance() + " VNĐ");
        }

        // Trừ tiền buyer
        buyerWallet.setBalance(buyerWallet.getBalance().subtract(order.getTotalAmount()));
        walletRepository.save(buyerWallet);

        // Ghi transaction buyer: trừ tiền mua hàng
        WalletTransaction buyerTx = new WalletTransaction();
        buyerTx.setWallet(buyerWallet);
        buyerTx.setType(TransactionType.PURCHASE);
        buyerTx.setAmount(order.getTotalAmount().negate()); // số âm = tiền ra
        buyerTx.setBalanceAfter(buyerWallet.getBalance());
        buyerTx.setReferenceType("ORDER");
        buyerTx.setReferenceId(order.getId());
        buyerTx.setDescription("Thanh toán đơn hàng " + order.getOrderCode());
        walletTransactionRepository.save(buyerTx);

        // Giữ tiền trong escrow của seller
        Wallet sellerWallet = walletRepository.findByUserIdForUpdate(order.getShop().getUser().getId())
                .orElse(null);
        if (sellerWallet == null) {
            // Tạo wallet cho seller nếu chưa có
            sellerWallet = new Wallet();
            sellerWallet.setUser(order.getShop().getUser());
            sellerWallet.setBalance(BigDecimal.ZERO);
            sellerWallet.setPendingBalance(BigDecimal.ZERO);
            sellerWallet = walletRepository.save(sellerWallet);
        }
        sellerWallet.setPendingBalance(sellerWallet.getPendingBalance().add(order.getTotalAmount()));
        walletRepository.save(sellerWallet);

        // Ghi transaction seller: tiền vào escrow (pending)
        WalletTransaction sellerTx = new WalletTransaction();
        sellerTx.setWallet(sellerWallet);
        sellerTx.setType(TransactionType.ESCROW_HOLD);
        sellerTx.setAmount(order.getTotalAmount()); // số dương = tiền vào pending
        sellerTx.setBalanceAfter(sellerWallet.getBalance()); // balance chưa đổi, chỉ pending đổi
        sellerTx.setReferenceType("ORDER");
        sellerTx.setReferenceId(order.getId());
        sellerTx.setDescription("Giữ tiền escrow đơn " + order.getOrderCode());
        walletTransactionRepository.save(sellerTx);

        // Cập nhật order
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.PAID);
        order.setPaidAt(LocalDateTime.now());
        order.setEscrowStatus(EscrowStatus.HOLDING);
        order.setAutoCompleteAt(LocalDateTime.now().plusDays(AUTO_COMPLETE_DAYS));
        orderRepository.save(order);

        addStatusHistory(order, OrderStatus.PENDING_PAYMENT, OrderStatus.PENDING, userId,
                "Đã thanh toán " + order.getTotalAmount() + " VNĐ — chờ seller xác nhận");
        return toOrderResponse(order);
    }

    // ===== XEM CHI TIẾT ĐƠN =====
    public OrderDTO.OrderResponse getOrderById(Long userId, Long orderId) {
        Order order = orderRepository.findByIdFull(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        // Chỉ buyer hoặc seller mới xem được
        boolean isBuyer = order.getBuyer().getId().equals(userId);
        boolean isSeller = order.getShop().getUser().getId().equals(userId);
        if (!isBuyer && !isSeller) {
            throw new BadRequestException("Bạn không có quyền xem đơn hàng này");
        }

        return toOrderResponse(order);
    }

    // ===== DANH SÁCH ĐƠN CỦA BUYER =====
    public ShopDTO.CursorPage<OrderDTO.OrderResponse> getMyOrders(
            Long userId, OrderStatus status, String cursor, int size) {
        size = Math.min(Math.max(size, 1), MAX_SIZE);
        Long cursorId = (cursor != null && !cursor.isEmpty()) ? Long.parseLong(cursor) : null;

        List<Order> orders = orderRepository.findByBuyerId(userId, status, cursorId, size + 1);
        boolean hasMore = orders.size() > size;
        if (hasMore) orders = orders.subList(0, size);

        List<OrderDTO.OrderResponse> items = orders.stream().map(this::toOrderResponse).collect(Collectors.toList());
        String nextCursor = hasMore && !orders.isEmpty()
                ? String.valueOf(orders.get(orders.size() - 1).getId()) : null;
        Long total = orderRepository.countByBuyerId(userId, status);

        return new ShopDTO.CursorPage<>(items, nextCursor, hasMore, items.size(), total);
    }

    // ===== DANH SÁCH ĐƠN CỦA SELLER =====
    public ShopDTO.CursorPage<OrderDTO.OrderResponse> getShopOrders(
            Long userId, OrderStatus status, String cursor, int size) {
        Shop shop = shopRepository.findByUserIdWithUser(userId)
                .orElseThrow(() -> new BadRequestException("Bạn chưa có shop"));

        size = Math.min(Math.max(size, 1), MAX_SIZE);
        Long cursorId = (cursor != null && !cursor.isEmpty()) ? Long.parseLong(cursor) : null;

        List<Order> orders = orderRepository.findByShopId(shop.getId(), status, cursorId, size + 1);
        boolean hasMore = orders.size() > size;
        if (hasMore) orders = orders.subList(0, size);

        List<OrderDTO.OrderResponse> items = orders.stream().map(this::toOrderResponse).collect(Collectors.toList());
        String nextCursor = hasMore && !orders.isEmpty()
                ? String.valueOf(orders.get(orders.size() - 1).getId()) : null;
        Long total = orderRepository.countByShopId(shop.getId(), status);

        return new ShopDTO.CursorPage<>(items, nextCursor, hasMore, items.size(), total);
    }

    // ===== SELLER XÁC NHẬN ĐƠN =====
    @Transactional
    public OrderDTO.OrderResponse confirmOrder(Long userId, Long orderId, OrderDTO.UpdateStatusRequest request) {
        Order order = getOrderForSeller(userId, orderId);
        validateStatusTransition(order.getStatus(), OrderStatus.CONFIRMED);
        order.setStatus(OrderStatus.CONFIRMED);
        if (request != null && request.getNote() != null) order.setSellerNote(request.getNote());
        orderRepository.save(order);
        addStatusHistory(order, OrderStatus.PENDING, OrderStatus.CONFIRMED, userId, "Seller xác nhận đơn");
        return toOrderResponse(order);
    }

    // ===== SELLER GỬI HÀNG =====
    @Transactional
    public OrderDTO.OrderResponse shipOrder(Long userId, Long orderId, OrderDTO.UpdateStatusRequest request) {
        Order order = getOrderForSeller(userId, orderId);
        validateStatusTransition(order.getStatus(), OrderStatus.SHIPPING);
        order.setStatus(OrderStatus.SHIPPING);
        order.setShippedAt(LocalDateTime.now());
        if (request != null) {
            if (request.getTrackingNumber() != null) order.setTrackingNumber(request.getTrackingNumber());
            if (request.getShippingMethod() != null) order.setShippingMethod(request.getShippingMethod());
        }
        orderRepository.save(order);
        addStatusHistory(order, OrderStatus.CONFIRMED, OrderStatus.SHIPPING, userId, "Đã gửi hàng");
        return toOrderResponse(order);
    }

    // ===== BUYER XÁC NHẬN ĐÃ NHẬN =====
    @Transactional
    public OrderDTO.OrderResponse confirmDelivered(Long userId, Long orderId) {
        Order order = getOrderForBuyer(userId, orderId);
        validateStatusTransition(order.getStatus(), OrderStatus.DELIVERED);
        order.setStatus(OrderStatus.DELIVERED);
        order.setDeliveredAt(LocalDateTime.now());
        order.setAutoCompleteAt(LocalDateTime.now().plusDays(AUTO_COMPLETE_DAYS));
        orderRepository.save(order);
        addStatusHistory(order, OrderStatus.SHIPPING, OrderStatus.DELIVERED, userId, "Buyer xác nhận đã nhận hàng");
        return toOrderResponse(order);
    }

    // ===== BUYER HOÀN THÀNH ĐƠN — giải phóng tiền cho seller =====
    @Transactional
    public OrderDTO.OrderResponse completeOrder(Long userId, Long orderId) {
        Order order = getOrderForBuyer(userId, orderId);
        validateStatusTransition(order.getStatus(), OrderStatus.COMPLETED);

        order.setStatus(OrderStatus.COMPLETED);
        order.setEscrowStatus(EscrowStatus.RELEASED);
        order.setEscrowReleasedAt(LocalDateTime.now());
        orderRepository.save(order);

        // Chuyển tiền từ pending sang balance cho seller
        Wallet sellerWallet = walletRepository.findByUserIdForUpdate(order.getShop().getUser().getId())
                .orElseThrow(() -> new BadRequestException("Không tìm thấy ví seller"));

        sellerWallet.setPendingBalance(sellerWallet.getPendingBalance().subtract(order.getTotalAmount()));
        sellerWallet.setBalance(sellerWallet.getBalance().add(order.getTotalAmount()));
        walletRepository.save(sellerWallet);

        // Ghi transaction: escrow released
        WalletTransaction releaseTx = new WalletTransaction();
        releaseTx.setWallet(sellerWallet);
        releaseTx.setType(TransactionType.ESCROW_RELEASE);
        releaseTx.setAmount(order.getTotalAmount());
        releaseTx.setBalanceAfter(sellerWallet.getBalance());
        releaseTx.setReferenceType("ORDER");
        releaseTx.setReferenceId(order.getId());
        releaseTx.setDescription("Nhận tiền đơn hàng " + order.getOrderCode());
        walletTransactionRepository.save(releaseTx);

        // Cập nhật sản phẩm → SOLD
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        int totalItemQty = 0;
        for (OrderItem oi : items) {
            Product p = oi.getProduct();
            p.setSoldCount(p.getSoldCount() + oi.getQuantity());
            // Chỉ SOLD khi quantity = 0 (đã trừ lúc checkout)
            // Nếu còn quantity > 0 thì giữ ACTIVE
            if (p.getQuantity() == 0 && p.getStatus() != ProductStatus.SOLD) {
                p.setStatus(ProductStatus.SOLD);
            }
            productRepository.save(p);
            totalItemQty += oi.getQuantity();
        }

        // Cập nhật thống kê
        User buyer = order.getBuyer();
        buyer.setTotalBought(buyer.getTotalBought() + totalItemQty);
        userRepository.save(buyer);

        User seller = order.getShop().getUser();
        seller.setTotalSold(seller.getTotalSold() + totalItemQty);
        userRepository.save(seller);

        Shop shop = order.getShop();
        shop.setTotalSold(shop.getTotalSold() + totalItemQty);
        shopRepository.save(shop);

        addStatusHistory(order, OrderStatus.DELIVERED, OrderStatus.COMPLETED, userId, "Đơn hàng hoàn thành");
        return toOrderResponse(order);
    }

    // ===== HỦY ĐƠN =====
    @Transactional
    public OrderDTO.OrderResponse cancelOrder(Long userId, Long orderId, OrderDTO.UpdateStatusRequest request) {
        Order order = orderRepository.findByIdFull(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        boolean isBuyer = order.getBuyer().getId().equals(userId);
        boolean isSeller = order.getShop().getUser().getId().equals(userId);
        if (!isBuyer && !isSeller) {
            throw new BadRequestException("Bạn không có quyền hủy đơn này");
        }

        // Chỉ hủy được khi PENDING hoặc CONFIRMED
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT
                && order.getStatus() != OrderStatus.PENDING
                && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new BadRequestException("Không thể hủy đơn ở trạng thái " + order.getStatus());
        }

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(OrderStatus.CANCELLED);
        if (request != null && request.getCancelReason() != null) {
            order.setCancelReason(request.getCancelReason());
        }
        orderRepository.save(order);

        // Hoàn lại sản phẩm → ACTIVE

        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        for (OrderItem oi : items) {
            Product p = oi.getProduct();
            p.setQuantity(p.getQuantity() + oi.getQuantity());
            if (p.getStatus() == ProductStatus.SOLD) {
                p.setStatus(ProductStatus.ACTIVE);
            }
            productRepository.save(p);
        }

        // Hoàn tiền nếu đã thanh toán
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            // Hoàn tiền cho buyer
            Wallet buyerWallet = walletRepository.findByUserIdForUpdate(order.getBuyer().getId())
                    .orElseThrow(() -> new BadRequestException("Không tìm thấy ví buyer"));
            buyerWallet.setBalance(buyerWallet.getBalance().add(order.getTotalAmount()));
            walletRepository.save(buyerWallet);

            WalletTransaction refundTx = new WalletTransaction();
            refundTx.setWallet(buyerWallet);
            refundTx.setType(TransactionType.REFUND);
            refundTx.setAmount(order.getTotalAmount());
            refundTx.setBalanceAfter(buyerWallet.getBalance());
            refundTx.setReferenceType("ORDER");
            refundTx.setReferenceId(order.getId());
            refundTx.setDescription("Hoàn tiền đơn hàng " + order.getOrderCode());
            walletTransactionRepository.save(refundTx);

            // Trừ pending của seller
            Wallet sellerWallet = walletRepository.findByUserIdForUpdate(order.getShop().getUser().getId())
                    .orElse(null);
            if (sellerWallet != null) {
                sellerWallet.setPendingBalance(
                        sellerWallet.getPendingBalance().subtract(order.getTotalAmount()));
                walletRepository.save(sellerWallet);

                WalletTransaction cancelTx = new WalletTransaction();
                cancelTx.setWallet(sellerWallet);
                cancelTx.setType(TransactionType.ESCROW_RELEASE);
                cancelTx.setAmount(order.getTotalAmount().negate());
                cancelTx.setBalanceAfter(sellerWallet.getBalance());
                cancelTx.setReferenceType("ORDER");
                cancelTx.setReferenceId(order.getId());
                cancelTx.setDescription("Hủy escrow đơn " + order.getOrderCode());
                walletTransactionRepository.save(cancelTx);
            }

            order.setPaymentStatus(PaymentStatus.REFUNDED);
            order.setEscrowStatus(EscrowStatus.REFUNDED);
        }

        addStatusHistory(order, oldStatus, OrderStatus.CANCELLED, userId,
                "Đơn bị hủy" + (request != null && request.getCancelReason() != null ? ": " + request.getCancelReason() : ""));
        return toOrderResponse(order);
    }

    // ===== HELPERS =====
    private Order getOrderForSeller(Long userId, Long orderId) {
        Order order = orderRepository.findByIdFull(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        if (!order.getShop().getUser().getId().equals(userId)) {
            throw new BadRequestException("Bạn không có quyền thao tác đơn này");
        }
        return order;
    }

    private Order getOrderForBuyer(Long userId, Long orderId) {
        Order order = orderRepository.findByIdFull(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        if (!order.getBuyer().getId().equals(userId)) {
            throw new BadRequestException("Bạn không có quyền thao tác đơn này");
        }
        return order;
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        Map<OrderStatus, List<OrderStatus>> allowed = Map.of(
                OrderStatus.PENDING_PAYMENT, List.of(OrderStatus.PENDING, OrderStatus.CANCELLED, OrderStatus.EXPIRED),
                OrderStatus.PENDING, List.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED),
                OrderStatus.CONFIRMED, List.of(OrderStatus.SHIPPING, OrderStatus.CANCELLED),
                OrderStatus.SHIPPING, List.of(OrderStatus.DELIVERED),
                OrderStatus.DELIVERED, List.of(OrderStatus.COMPLETED, OrderStatus.RETURN_REQUESTED)
        );
        if (!allowed.getOrDefault(current, List.of()).contains(next)) {
            throw new BadRequestException("Không thể chuyển từ " + current + " sang " + next);
        }
    }

    private void addStatusHistory(Order order, OrderStatus from, OrderStatus to, Long changedBy, String note) {
        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(order);
        history.setFromStatus(from != null ? from.name() : null);
        history.setToStatus(to.name());
        if (changedBy != null) {
            history.setChangedBy(userRepository.findById(changedBy).orElse(null));
        }
        history.setNote(note);
        statusHistoryRepository.save(history);
    }

    private String generateOrderCode() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = String.format("%04d", new Random().nextInt(10000));
        return "ORD-" + date + "-" + random;
    }

    private OrderDTO.OrderResponse toOrderResponse(Order o) {
        OrderDTO.OrderResponse r = new OrderDTO.OrderResponse();
        r.setId(o.getId());
        r.setOrderCode(o.getOrderCode());
        r.setStatus(o.getStatus().name());
        r.setPaymentMethod(o.getPaymentMethod() != null ? o.getPaymentMethod().name() : null);
        r.setPaymentStatus(o.getPaymentStatus() != null ? o.getPaymentStatus().name() : null);
        r.setEscrowStatus(o.getEscrowStatus() != null ? o.getEscrowStatus().name() : null);
        r.setSubtotal(o.getSubtotal());
        r.setShippingFee(o.getShippingFee());
        r.setDiscountAmount(o.getDiscountAmount());
        r.setTotalAmount(o.getTotalAmount());
        r.setShippingName(o.getShippingName());
        r.setShippingPhone(o.getShippingPhone());
        r.setShippingAddress(o.getShippingAddress());
        r.setShippingMethod(o.getShippingMethod());
        r.setTrackingNumber(o.getTrackingNumber());
        r.setBuyerNote(o.getBuyerNote());
        r.setSellerNote(o.getSellerNote());
        r.setCancelReason(o.getCancelReason());
        r.setCreatedAt(o.getCreatedAt());
        r.setPaidAt(o.getPaidAt());
        r.setShippedAt(o.getShippedAt());
        r.setDeliveredAt(o.getDeliveredAt());
        r.setAutoCompleteAt(o.getAutoCompleteAt());

        // Items
        // Items + tính tổng quantity
        List<OrderItem> items = orderItemRepository.findByOrderId(o.getId());
        int totalQty = 0;
        List<OrderDTO.OrderItemInfo> itemInfos = new ArrayList<>();
        for (OrderItem oi : items) {
            OrderDTO.OrderItemInfo ii = new OrderDTO.OrderItemInfo();
            ii.setId(oi.getId());
            ii.setProductId(oi.getProduct().getId());
            ii.setProductName(oi.getProductName());
            ii.setProductImage(oi.getProductImage());
            ii.setPrice(oi.getPrice());
            ii.setQuantity(oi.getQuantity());
            itemInfos.add(ii);
            totalQty += oi.getQuantity();
        }
        r.setItems(itemInfos);
        r.setTotalQuantity(totalQty);

        // Shop
        Shop shop = o.getShop();
        OrderDTO.ShopSummary ss = new OrderDTO.ShopSummary();
        ss.setId(shop.getId()); ss.setShopName(shop.getShopName()); ss.setSlug(shop.getSlug());
        r.setShop(ss);

        // Buyer
        User buyer = o.getBuyer();
        OrderDTO.BuyerSummary bs = new OrderDTO.BuyerSummary();
        bs.setId(buyer.getId()); bs.setUsername(buyer.getUsername()); bs.setFullName(buyer.getFullName());
        r.setBuyer(bs);

        // Status history
        List<OrderStatusHistory> history = statusHistoryRepository.findByOrderIdOrderByCreatedAtAsc(o.getId());
        r.setStatusHistory(history.stream().map(h -> {
            OrderDTO.StatusHistoryInfo hi = new OrderDTO.StatusHistoryInfo();
            hi.setFromStatus(h.getFromStatus()); hi.setToStatus(h.getToStatus());
            hi.setNote(h.getNote()); hi.setCreatedAt(h.getCreatedAt());
            return hi;
        }).collect(Collectors.toList()));

        return r;
    }

    // ===== XEM VÍ =====
    public OrderDTO.WalletResponse getWallet(Long userId) {
        Wallet wallet = walletRepository.findByUserId(userId).orElse(null);
        OrderDTO.WalletResponse r = new OrderDTO.WalletResponse();
        if (wallet != null) {
            r.setBalance(wallet.getBalance());
            r.setPendingBalance(wallet.getPendingBalance());
        } else {
            r.setBalance(BigDecimal.ZERO);
            r.setPendingBalance(BigDecimal.ZERO);
        }
        return r;
    }

    // ===== NẠP TIỀN TEST =====
    @Transactional
    public OrderDTO.WalletResponse deposit(Long userId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Số tiền nạp phải lớn hơn 0");
        }

        Wallet wallet = walletRepository.findByUserIdForUpdate(userId).orElse(null);
        if (wallet == null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
            wallet = new Wallet();
            wallet.setUser(user);
            wallet.setBalance(BigDecimal.ZERO);
            wallet.setPendingBalance(BigDecimal.ZERO);
            wallet = walletRepository.save(wallet);
        }

        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);

        WalletTransaction tx = new WalletTransaction();
        tx.setWallet(wallet);
        tx.setType(TransactionType.DEPOSIT);
        tx.setAmount(amount);
        tx.setBalanceAfter(wallet.getBalance());
        tx.setDescription("Nạp tiền vào ví");
        walletTransactionRepository.save(tx);

        OrderDTO.WalletResponse r = new OrderDTO.WalletResponse();
        r.setBalance(wallet.getBalance());
        r.setPendingBalance(wallet.getPendingBalance());
        return r;
    }

    // ===== LỊCH SỬ GIAO DỊCH =====
    public ShopDTO.CursorPage<OrderDTO.WalletTransactionInfo> getWalletTransactions(
            Long userId, String cursor, int size) {
        Wallet wallet = walletRepository.findByUserId(userId).orElse(null);
        if (wallet == null) {
            return new ShopDTO.CursorPage<>(List.of(), null, false, 0, 0L);
        }

        size = Math.min(Math.max(size, 1), 50);
        Long cursorId = (cursor != null && !cursor.isEmpty()) ? Long.parseLong(cursor) : null;

        List<WalletTransaction> txs = walletTransactionRepository
                .findByWalletId(wallet.getId(), cursorId, size + 1);

        boolean hasMore = txs.size() > size;
        if (hasMore) txs = txs.subList(0, size);

        List<OrderDTO.WalletTransactionInfo> items = txs.stream().map(tx -> {
            OrderDTO.WalletTransactionInfo info = new OrderDTO.WalletTransactionInfo();
            info.setId(tx.getId());
            info.setType(tx.getType().name());
            info.setAmount(tx.getAmount());
            info.setBalanceAfter(tx.getBalanceAfter());
            info.setDescription(tx.getDescription());
            info.setCreatedAt(tx.getCreatedAt());
            return info;
        }).collect(Collectors.toList());

        String nextCursor = hasMore && !txs.isEmpty()
                ? String.valueOf(txs.get(txs.size() - 1).getId()) : null;
        Long total = walletTransactionRepository.countByWalletId(wallet.getId());

        return new ShopDTO.CursorPage<>(items, nextCursor, hasMore, items.size(), total);
    }
}
