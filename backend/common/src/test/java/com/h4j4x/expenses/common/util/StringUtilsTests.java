package com.h4j4x.expenses.common.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class StringUtilsTests {
    @Test
    public void testFirstNotBlank() {
        assertNull(StringUtils.firstNotBlank(null, null));
        assertEquals("zero", StringUtils.firstNotBlank("zero", "first", "second"));
        assertEquals("first", StringUtils.firstNotBlank(null, "first", "second"));
        assertEquals("second", StringUtils.firstNotBlank(null, null, "second"));
        assertEquals("first", StringUtils.firstNotBlank(" ", "first", "second"));
        assertEquals("second", StringUtils.firstNotBlank("", " ", "second"));
    }
}
