package com.example.carnest.Service;

import com.example.carnest.Entity.*;
import com.example.carnest.Enum.CriteriaType;
import com.example.carnest.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BadgeService {

    @Autowired private BadgeRepository badgeRepository;
    @Autowired private UserBadgeRepository userBadgeRepository;
    @Autowired private UserRepository userRepository;

    public List<Map<String, Object>> getUserBadges(Long userId) {
        List<UserBadge> ubs = userBadgeRepository.findByUserIdOrderByEarnedAtDesc(userId);
        return ubs.stream().map(ub -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", ub.getBadge().getId());
            m.put("name", ub.getBadge().getName());
            m.put("description", ub.getBadge().getDescription());
            m.put("iconUrl", ub.getBadge().getIconUrl());
            m.put("tier", ub.getBadge().getTier().name());
            m.put("isFeatured", ub.getIsFeatured());
            m.put("earnedAt", ub.getEarnedAt());
            return m;
        }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> getAllBadges() {
        return badgeRepository.findAll().stream()
                .filter(b -> b.getIsActive())
                .map(b -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", b.getId());
                    m.put("name", b.getName());
                    m.put("description", b.getDescription());
                    m.put("iconUrl", b.getIconUrl());
                    m.put("tier", b.getTier().name());
                    m.put("criteriaType", b.getCriteriaType().name());
                    m.put("criteriaValue", b.getCriteriaValue());
                    return m;
                }).collect(Collectors.toList());
    }

    // Chạy mỗi 10 phút — kiểm tra và cấp badge
    @Scheduled(fixedRate = 600000)
    @Transactional
    public void checkAndAwardBadges() {
        List<Badge> badges = badgeRepository.findAll().stream()
                .filter(Badge::getIsActive).collect(Collectors.toList());
        List<User> users = userRepository.findAll();

        for (User user : users) {
            for (Badge badge : badges) {
                if (userBadgeRepository.existsByUserIdAndBadgeId(user.getId(), badge.getId())) continue;

                boolean earned = false;
                switch (badge.getCriteriaType()) {
                    case TOTAL_BOUGHT: earned = user.getTotalBought() >= badge.getCriteriaValue(); break;
                    case TOTAL_SOLD: earned = user.getTotalSold() >= badge.getCriteriaValue(); break;
                    case RATING_AVG: earned = user.getSellerRatingAvg().doubleValue() >= badge.getCriteriaValue(); break;
                    default: break;
                }

                if (earned) {
                    UserBadge ub = new UserBadge();
                    ub.setUser(user);
                    ub.setBadge(badge);
                    ub.setIsFeatured(false);
                    userBadgeRepository.save(ub);
                }
            }
        }
    }
}