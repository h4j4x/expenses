package com.h4j4x.expenses.common.security.impl;

import com.github.javafaker.Faker;
import com.h4j4x.expenses.common.security.StringHasher;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import static org.junit.jupiter.api.Assertions.*;

public class PBKDF2StringHasherTests {
    private final Faker faker = new Faker(new Locale("en-US"));

    @TestFactory
    List<DynamicTest> testHashMatches() {
        StringHasher stringHasher = new PBKDF2StringHasher(512, 128);
        List<DynamicTest> tests = new ArrayList<>();
        var salt = stringHasher.salt();
        for (int i = 10; i <= 100; i += 10) {
            var string = faker.random().hex(i);
            tests.add(testHashing(stringHasher, string, salt));
        }
        return tests;
    }

    private DynamicTest testHashing(StringHasher stringHasher, String string, String salt) {
        return DynamicTest.dynamicTest("Test " + string + " hash",
            () -> {
                var hash = stringHasher.hash(string, salt);
                assertNotEquals(string, hash);
                assertTrue(stringHasher.match(string, salt, hash));
                assertFalse(stringHasher.match(string + "a", salt, hash));
            });
    }
}
