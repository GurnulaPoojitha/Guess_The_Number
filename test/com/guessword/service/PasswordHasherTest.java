package com.guessword.service;

import com.guessword.Test;

import static com.guessword.Assert.assertFalse;
import static com.guessword.Assert.assertTrue;

public class PasswordHasherTest {

    @Test
    void correctPasswordMatches() {
        String stored = PasswordHasher.hash("abc$1");
        assertTrue(PasswordHasher.matches("abc$1", stored), "the right password should match");
    }

    @Test
    void wrongPasswordDoesNotMatch() {
        String stored = PasswordHasher.hash("abc$1");
        assertFalse(PasswordHasher.matches("abc$2", stored), "a wrong password must not match");
        assertFalse(PasswordHasher.matches(null, stored), "null must not match");
    }

    @Test
    void passwordIsNotStoredInPlainTextAndIsSalted() {
        String a = PasswordHasher.hash("abc$1");
        String b = PasswordHasher.hash("abc$1");
        assertFalse(a.contains("abc$1"), "hash must not contain the password");
        assertFalse(a.equals(b), "two hashes of the same password should differ (random salt)");
    }

    @Test
    void malformedStoredValueNeverMatches() {
        assertFalse(PasswordHasher.matches("abc$1", "not-a-hash"), "garbage must not match");
    }
}
