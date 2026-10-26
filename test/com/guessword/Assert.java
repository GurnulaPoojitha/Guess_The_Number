package com.guessword;

import java.util.Objects;

/** Minimal assertions for the tests. */
public final class Assert {

    private Assert() {
    }

    public static void assertEquals(Object expected, Object actual) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError("expected <" + expected + "> but was <" + actual + ">");
        }
    }

    public static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    public static void assertFalse(boolean condition, String message) {
        assertTrue(!condition, message);
    }

    public static void assertNull(Object value) {
        if (value != null) {
            throw new AssertionError("expected null but was <" + value + ">");
        }
    }

    public static void assertNotNull(Object value) {
        if (value == null) {
            throw new AssertionError("expected a value but was null");
        }
    }

    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }

    /** Checks that {@code code} throws {@code type}, and that the message contains {@code messagePart}. */
    public static <T extends Throwable> T assertThrows(Class<T> type, String messagePart, ThrowingRunnable code) {
        try {
            code.run();
        } catch (Throwable t) {
            if (!type.isInstance(t)) {
                throw new AssertionError("expected " + type.getSimpleName() + " but got " + t, t);
            }
            if (messagePart != null && (t.getMessage() == null || !t.getMessage().contains(messagePart))) {
                throw new AssertionError("expected message containing '" + messagePart + "' but was '"
                        + t.getMessage() + "'");
            }
            return type.cast(t);
        }
        throw new AssertionError("expected " + type.getSimpleName() + " to be thrown, but nothing was thrown");
    }
}
