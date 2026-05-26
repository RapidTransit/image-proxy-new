package com.pss.image.proxy.util;

public final class Verify {
    private Verify() {
    }

    public static void isTrue(boolean check, String message) {
        if (!check) {
            throw new IllegalStateException(message);
        }
    }

    public static void isFalse(boolean check, String message) {
        if (check) {
            throw new IllegalStateException(message);
        }
    }
}
