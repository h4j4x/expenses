package com.h4j4x.expenses.common.util;

import com.github.javafaker.Faker;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class NumberUtilsTests {
    private final Faker faker = new Faker(new Locale("en-US"));

    @TestFactory
    List<DynamicTest> testParseLong() {
        List<DynamicTest> tests = new ArrayList<>();
        for (int i = 10; i <= 100; i += 10) {
            tests.add(testParseLong(faker.random().nextLong()));
        }
        return tests;
    }

    private DynamicTest testParseLong(long value) {
        return DynamicTest.dynamicTest("Test NumberUtils.parseLong",
            () -> {
                var parsed = NumberUtils.parseLong(String.valueOf(value));
                assertEquals(value, parsed);
            });
    }
}
