package com.Twitter.com.Services.utility;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OTPGeneratorTest {

    @Test
    void otpIsSixDigitsNumeric() {
        String otp = OTPGenerator.generateOTP();

        assertEquals(6, otp.length());
        assertTrue(otp.chars().allMatch(Character::isDigit));
    }

    @Test
    void successiveOtpsAreUsuallyDifferent() {
        String otp1 = OTPGenerator.generateOTP();
        String otp2 = OTPGenerator.generateOTP();

        // It's possible to collide, but extremely unlikely; this guards basic randomness.
        assertNotEquals(otp1, otp2);
    }
}
