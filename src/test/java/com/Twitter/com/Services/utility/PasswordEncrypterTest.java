package com.Twitter.com.Services.utility;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordEncrypterTest {

    @Test
    void hashesAreDeterministicWithSecret() throws Exception {
        String h1 = PasswordEncrypter.hashPasswordWithStaticSecret("password");
        String h2 = PasswordEncrypter.hashPasswordWithStaticSecret("password");

        assertEquals(h1, h2, "Same input should yield same hash");
    }

    @Test
    void hashesDifferForDifferentPasswords() throws Exception {
        String h1 = PasswordEncrypter.hashPasswordWithStaticSecret("password1");
        String h2 = PasswordEncrypter.hashPasswordWithStaticSecret("password2");

        assertNotEquals(h1, h2, "Different inputs should yield different hashes");
    }

    @Test
    void verifyPasswordMatchesCorrectHash() throws Exception {
        String hash = PasswordEncrypter.hashPasswordWithStaticSecret("secret123");

        assertTrue(PasswordEncrypter.verifyPasswordWithStaticSecret("secret123", hash));
        assertFalse(PasswordEncrypter.verifyPasswordWithStaticSecret("wrong", hash));
    }
}
