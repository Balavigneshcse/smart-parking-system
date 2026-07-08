package com.smart.parking.util;

import java.util.regex.Pattern;

public final class PasswordValidator {
    // Min 8 chars, 1 uppercase, 1 lowercase, 1 digit, 1 special char
    private static final Pattern STRONG =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#^()_+\\-=]).{8,}$");

    private PasswordValidator() {}

    public static boolean isStrong(String password) {
        if (password == null) return false;
        return STRONG.matcher(password).matches();
    }

    public static String strengthMessage() {
        return "Password must be at least 8 characters with uppercase, lowercase, digit, and special character.";
    }
}
