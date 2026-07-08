package com.smart.parking.util;

import java.util.regex.Pattern;

public final class PlateNumberValidator {
    // Indian vehicle number format: TN09AB1234 (spaces optional, stored without spaces)
    private static final Pattern PATTERN =
            Pattern.compile("^[A-Z]{2}\\s*[0-9]{2}\\s*[A-Z]{1,3}\\s*[0-9]{1,4}$");

    private PlateNumberValidator() {}

    public static boolean isValid(String plate) {
        if (plate == null) return false;
        return PATTERN.matcher(plate.trim().toUpperCase()).matches();
    }

    /** Normalise to uppercase without spaces: "TN 09 AB 1234" → "TN09AB1234" */
    public static String normalise(String plate) {
        if (plate == null) return null;
        return plate.trim().toUpperCase().replaceAll("\\s+", "");
    }
}
