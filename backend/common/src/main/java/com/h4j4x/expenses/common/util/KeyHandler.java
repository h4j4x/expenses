package com.h4j4x.expenses.common.util;

public class KeyHandler {
    private final String separator;

    public KeyHandler(String separator) {
        this.separator = separator;
    }

    public String createKey(Long prefix, Long suffix) {
        return String.format("%d%s%d", prefix, separator, suffix);
    }

    public Long parsePrefix(String key) {
        return parseLong(key, 0);
    }

    public Long parseSuffix(String key) {
        return parseLong(key, 1);
    }

    private Long parseLong(String key, int index) {
        if (index >= 0 && key != null) {
            String[] parts = key.split(separator);
            if (parts.length > index) {
                return NumberUtils.parseLong(parts[index]);
            }
        }
        return null;
    }
}
