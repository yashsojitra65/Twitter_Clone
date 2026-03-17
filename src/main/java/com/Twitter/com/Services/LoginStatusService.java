package com.Twitter.com.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class LoginStatusService {
    private static final String KEY_PREFIX = "login-status:";
    private static final Duration TTL = Duration.ofHours(24);
    private final StringRedisTemplate redisTemplate;

    public void markLogin(String email) {
        redisTemplate.opsForValue().set(KEY_PREFIX + email, "login", TTL);
    }

    public void markLogout(String email) {
        redisTemplate.delete(KEY_PREFIX + email);
    }

    public boolean isLoggedIn(String email) {
        String status = redisTemplate.opsForValue().get(KEY_PREFIX + email);
        return "login".equalsIgnoreCase(status);
    }
}
