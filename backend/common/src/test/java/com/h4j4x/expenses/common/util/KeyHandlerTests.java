package com.h4j4x.expenses.common.util;

import com.github.javafaker.Faker;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class KeyHandlerTests {
    private final Faker faker = new Faker(new Locale("en-US"));

    @TestFactory
    List<DynamicTest> testCreateAndParseKey() {
        KeyHandler keyHandler = new KeyHandler("-" + faker.app().name() + "-");
        List<DynamicTest> tests = new ArrayList<>();
        for (int i = 10; i <= 100; i += 10) {
            var prefix = faker.random().nextLong();
            var suffix = faker.random().nextLong();
            tests.add(testCreateAndParseKey(keyHandler, prefix, suffix));
        }
        return tests;
    }

    private DynamicTest testCreateAndParseKey(KeyHandler keyHandler, long prefix, long suffix) {
        return DynamicTest.dynamicTest("Test KeyHandler.createKey",
            () -> {
                var key = keyHandler.createKey(prefix, suffix);
                assertNotNull(key);
                assertEquals(prefix, keyHandler.parsePrefix(key));
                assertEquals(suffix, keyHandler.parseSuffix(key));
            });
    }
}
