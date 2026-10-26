package com.guessword.service;

import com.guessword.Test;

import static com.guessword.Assert.assertFalse;
import static com.guessword.Assert.assertTrue;

public class ValidationRulesTest {

    @Test
    void acceptsValidUsernames() {
        for (String name : new String[]{"PlayerOne", "Alice", "aBcDe", "JohnSmith", "ZZZZz"}) {
            assertTrue(ValidationRules.isValidUsername(name), name + " should be accepted");
        }
    }

    @Test
    void rejectsInvalidUsernames() {
        String[] bad = {
                null, "",
                "Abcd",        // only 4 letters
                "alice",       // no upper case
                "ALICE",       // no lower case
                "Alice1",      // digit
                "Alice Smith", // space
                "Alice_",      // symbol
        };
        for (String name : bad) {
            assertFalse(ValidationRules.isValidUsername(name), name + " should be rejected");
        }
    }

    @Test
    void acceptsValidPasswords() {
        for (String pw : new String[]{"abc$1", "game$1", "Pass%99", "a1*bc", "x9@yz", "Admin$123"}) {
            assertTrue(ValidationRules.isValidPassword(pw), pw + " should be accepted");
        }
    }

    @Test
    void rejectsInvalidPasswords() {
        String[] bad = {
                null, "",
                "a1$b",   // only 4 characters
                "abcde1", // no special character
                "abcde$", // no digit
                "12345$", // no letter
                "abc#12", // # is not one of the allowed special characters
        };
        for (String pw : bad) {
            assertFalse(ValidationRules.isValidPassword(pw), pw + " should be rejected");
        }
    }

    @Test
    void wordsMustBeFiveUpperCaseLetters() {
        for (String w : new String[]{"TOWER", "APPLE", "ZZZZZ"}) {
            assertTrue(ValidationRules.isValidWord(w), w + " should be accepted");
        }
        for (String w : new String[]{null, "", "tower", "Tower", "TOWE", "TOWERS", "T0WER", "TOW R", "TÖWER"}) {
            assertFalse(ValidationRules.isValidWord(w), w + " should be rejected");
        }
    }
}
