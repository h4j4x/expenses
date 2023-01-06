package com.h4j4x.expenses.common.util;

import java.math.BigDecimal;

public class NumberUtils {
    public static Long parseLong(String value) {
        if (value != null) {
            try {
                var bigDecimal = new BigDecimal(value);
                return bigDecimal.longValueExact();
            } catch (NumberFormatException | ArithmeticException ignored) {
            }
        }
        return null;
    }
}
