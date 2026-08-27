package com.hunghv.inmobitestjava.utils;

import lombok.experimental.UtilityClass;

import java.util.Locale;
import java.util.Objects;

@UtilityClass
public class EmailUtils {

    public static String normalizeEmail(String email) {
        return Objects.requireNonNull(email, "email must not be null")
            .trim()
            .toLowerCase(Locale.ROOT);
    }
}
