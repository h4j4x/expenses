package com.h4j4x.expenses.common.util;

public class ObjectUtils {
    @SafeVarargs
    public static <T> T firstNotNull(T... values) {
        if (values != null) {
            for (T value : values) {
                if (value != null) {
                    return value;
                }
            }
        }
        return null;
    }
}
