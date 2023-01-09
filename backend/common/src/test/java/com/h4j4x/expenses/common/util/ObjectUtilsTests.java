package com.h4j4x.expenses.common.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ObjectUtilsTests {
    @Test
    public void testFirstNotNull() {
        assertNull(ObjectUtils.<String>firstNotNull(null, null));
        assertEquals("zero", ObjectUtils.firstNotNull("zero", "first", "second"));
        assertEquals("first", ObjectUtils.firstNotNull(null, "first", "second"));
        assertEquals("second", ObjectUtils.firstNotNull(null, null, "second"));
        assertEquals(2L, ObjectUtils.firstNotNull(null, null, 2L));
    }
}
