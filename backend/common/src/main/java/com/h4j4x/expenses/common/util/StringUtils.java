package com.h4j4x.expenses.common.util;

public class StringUtils {
    public static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    public static String firstNotBlank(String... values) {
        if (values != null) {
            for (String value : values) {
                if (isNotBlank(value)) {
                    return value;
                }
            }
        }
        return null;
    }
}
