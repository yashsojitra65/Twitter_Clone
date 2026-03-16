package com.Twitter.com.Services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock
    StringRedisTemplate redisTemplate;
    @Mock
    ValueOperations<String, String> valueOperations;

    private OtpService otpService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        otpService = new OtpService(redisTemplate);
    }

    @Test
    void storeOtpUsesFiveMinuteTtl() {
        otpService.storeOtp("user@example.com", "123456");

        verify(valueOperations).set(eq("otp:user@example.com"), eq("123456"), eq(Duration.ofMinutes(5)));
    }

    @Test
    void validateOtpDeletesValueOnMatch() {
        when(valueOperations.get("otp:user@example.com")).thenReturn("123456");

        boolean valid = otpService.validateOtp("user@example.com", "123456");

        assertTrue(valid);
        verify(redisTemplate).delete("otp:user@example.com");
    }

    @Test
    void validateOtpReturnsFalseWhenMissing() {
        when(valueOperations.get("otp:user@example.com")).thenReturn(null);

        boolean valid = otpService.validateOtp("user@example.com", "000000");

        assertFalse(valid);
        verify(redisTemplate, never()).delete(anyString());
    }
}
