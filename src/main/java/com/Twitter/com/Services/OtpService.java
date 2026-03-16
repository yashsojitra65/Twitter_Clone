package com.Twitter.com.Services;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class OtpService {
    private static final String KEY_PREFIX = "otp:";
    private static final Duration OTP_TTL = Duration.ofMinutes(5);
    private final StringRedisTemplate redisTemplate;

    public OtpService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void storeOtp(String email, String otp) {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        ops.set(KEY_PREFIX + email, otp, OTP_TTL);
    }

    public boolean validateOtp(String email, String otp) {
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        String cached = ops.get(KEY_PREFIX + email);
        if (cached != null && cached.equals(otp)) {
            redisTemplate.delete(KEY_PREFIX + email);
            return true;
        }
        return false;
    }
}
