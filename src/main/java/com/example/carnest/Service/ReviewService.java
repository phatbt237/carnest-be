package com.example.carnest.Service;

import com.example.carnest.Entity.*;
import com.example.carnest.Enum.*;
import com.example.carnest.Exception.BadRequestException;
import com.example.carnest.Exception.ResourceNotFoundException;
import com.example.carnest.Model.ReviewDTO;
import com.example.carnest.Model.ShopDTO;
import com.example.carnest.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    @Autowired private ReviewRepository reviewRepository;
    @Autowired private ReviewImageRepository reviewImageRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ShopRepository shopRepository;

    @Transactional
    public ReviewDTO.ReviewResponse createReview(Long userId, ReviewDTO.CreateRequest request) {
        Order order = orderRepository.findByIdFull(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", request.getOrderId()));

        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new BadRequestException("Chỉ đánh giá được đơn hàng đã hoàn thành");
        }
        if (reviewRepository.existsByOrderIdAndReviewerId(request.getOrderId(), userId)) {
            throw new BadRequestException("Bạn đã đánh giá đơn hàng này rồi");
        }

        boolean isBuyer = order.getBuyer().getId().equals(userId);
        boolean isSeller = order.getShop().getUser().getId().equals(userId);
        if (!isBuyer && !isSeller) {
            throw new BadRequestException("Bạn không thuộc đơn hàng này");
        }

        User reviewer = userRepository.findById(userId).orElseThrow();
        User reviewed = isBuyer ? order.getShop().getUser() : order.getBuyer();

        Review review = new Review();
        review.setOrder(order);
        review.setReviewer(reviewer);
        review.setReviewed(reviewed);
        review.setType(isBuyer ? ReviewType.BUYER_TO_SELLER : ReviewType.SELLER_TO_BUYER);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setRatingAccuracy(request.getRatingAccuracy());
        review.setRatingShipping(request.getRatingShipping());
        review.setRatingCommunication(request.getRatingCommunication());
        review.setIsHidden(false);
        review = reviewRepository.save(review);

        if (request.getImageUrls() != null) {
            for (String url : request.getImageUrls()) {
                ReviewImage img = new ReviewImage();
                img.setReview(review);
                img.setImageUrl(url);
                reviewImageRepository.save(img);
            }
        }

        // Cập nhật rating trung bình
        updateUserRating(reviewed.getId(), review.getType());

        return toResponse(review);
    }

    @Transactional
    public ReviewDTO.ReviewResponse replyReview(Long userId, Long reviewId, ReviewDTO.ReplyRequest request) {
        Review review = reviewRepository.findByIdFull(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));
        if (!review.getReviewed().getId().equals(userId)) {
            throw new BadRequestException("Bạn chỉ có thể phản hồi đánh giá về mình");
        }
        if (review.getReply() != null) {
            throw new BadRequestException("Bạn đã phản hồi đánh giá này rồi");
        }
        review.setReply(request.getReply());
        review.setRepliedAt(LocalDateTime.now());
        reviewRepository.save(review);
        return toResponse(review);
    }

    public ShopDTO.CursorPage<ReviewDTO.ReviewResponse> getShopReviews(Long shopId, String cursor, int size) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop", "id", shopId));
        Long sellerId = shop.getUser().getId();
        size = Math.min(Math.max(size, 1), 50);
        Long cursorId = (cursor != null && !cursor.isEmpty()) ? Long.parseLong(cursor) : null;

        List<Review> reviews = reviewRepository.findByShopSellerId(sellerId, cursorId, size + 1);
        boolean hasMore = reviews.size() > size;
        if (hasMore) reviews = reviews.subList(0, size);

        List<ReviewDTO.ReviewResponse> items = reviews.stream().map(this::toResponse).collect(Collectors.toList());
        String nextCursor = hasMore && !reviews.isEmpty() ? String.valueOf(reviews.get(reviews.size() - 1).getId()) : null;
        Long total = reviewRepository.countByReviewedId(sellerId, ReviewType.BUYER_TO_SELLER);

        return new ShopDTO.CursorPage<>(items, nextCursor, hasMore, items.size(), total);
    }

    private void updateUserRating(Long userId, ReviewType type) {
        Double avg = reviewRepository.avgRatingByReviewedId(userId, type);
        if (avg != null) {
            User user = userRepository.findById(userId).orElseThrow();
            if (type == ReviewType.BUYER_TO_SELLER) {
                user.setSellerRatingAvg(BigDecimal.valueOf(avg));
            } else {
                user.setBuyerRatingAvg(BigDecimal.valueOf(avg));
            }
            userRepository.save(user);
        }
    }

    private ReviewDTO.ReviewResponse toResponse(Review r) {
        ReviewDTO.ReviewResponse res = new ReviewDTO.ReviewResponse();
        res.setId(r.getId());
        res.setOrderId(r.getOrder().getId());
        res.setType(r.getType().name());
        res.setRating(r.getRating());
        res.setComment(r.getComment());
        res.setRatingAccuracy(r.getRatingAccuracy());
        res.setRatingShipping(r.getRatingShipping());
        res.setRatingCommunication(r.getRatingCommunication());
        res.setReply(r.getReply());
        res.setRepliedAt(r.getRepliedAt());
        res.setReviewerUsername(r.getReviewer().getUsername());
        res.setReviewerAvatar(r.getReviewer().getAvatarUrl());
        res.setReviewedUsername(r.getReviewed().getUsername());
        res.setCreatedAt(r.getCreatedAt());
        List<ReviewImage> imgs = reviewImageRepository.findByReviewId(r.getId());
        res.setImageUrls(imgs.stream().map(ReviewImage::getImageUrl).collect(Collectors.toList()));
        return res;
    }
}