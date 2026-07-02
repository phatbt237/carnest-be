package com.example.carnest.Config;

import com.example.carnest.Exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;

@Aspect
@Component
public class RateLimitAspect {

    @Autowired private StringRedisTemplate redisTemplate;

    @Around("@annotation(rateLimit)")
    public Object enforce(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String key = "rl:" + rateLimit.action() + ":" + resolveIdentity(rateLimit.keyType());

        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, Duration.ofSeconds(rateLimit.windowSeconds()));
        }
        if (count != null && count > rateLimit.limit()) {
            long retryAfterSeconds = resolveRetryAfterSeconds(key, rateLimit.windowSeconds());
            throw new RateLimitExceededException(
                    "Bạn thao tác quá nhanh, vui lòng thử lại sau " + formatDuration(retryAfterSeconds),
                    retryAfterSeconds);
        }

        return joinPoint.proceed();
    }

    private long resolveRetryAfterSeconds(String key, int windowSeconds) {
        Long ttl = redisTemplate.getExpire(key);
        return (ttl != null && ttl > 0) ? ttl : windowSeconds;
    }

    private String formatDuration(long seconds) {
        if (seconds < 60) {
            return seconds + " giây";
        }
        long minutes = seconds / 60;
        long remainSeconds = seconds % 60;
        return remainSeconds == 0 ? minutes + " phút" : minutes + " phút " + remainSeconds + " giây";
    }

    private String resolveIdentity(RateLimit.KeyType keyType) {
        HttpServletRequest request = currentRequest();

        if (keyType == RateLimit.KeyType.IP) {
            return resolveIp(request);
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            return "u" + userDetails.getUserId();
        }
        return resolveIp(request);
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs.getRequest();
    }

    private String resolveIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
