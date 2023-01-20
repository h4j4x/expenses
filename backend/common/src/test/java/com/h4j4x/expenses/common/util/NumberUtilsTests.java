package com.h4j4x.expenses.common.util;

import com.github.javafaker.Faker;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class NumberUtilsTests {
    private final Faker faker = new Faker(new Locale("en-US"));

    @TestFactory
    List<DynamicTest> whenParseValidLong_Then_ShouldReturnValue() {
        List<DynamicTest> tests = new ArrayList<>();
        for (int i = 10; i <= 100; i += 10) {
            tests.add(parseValidLong(faker.random().nextLong()));
        }
        return tests;
    }

    private DynamicTest parseValidLong(long value) {
        return DynamicTest.dynamicTest("Test NumberUtils.parseLong",
            () -> {
                var parsed = NumberUtils.parseLong(String.valueOf(value));
                assertEquals(value, parsed);
            });
    }

    @Test
    public void whenParseInvalidLong_Then_ShouldReturnNull() {
        assertNull(NumberUtils.parseLong("-"));
    }

    @Test
    public void whenParseNull_Then_ShouldReturnNull() {
        assertNull(NumberUtils.parseLong(null));
    }
}
