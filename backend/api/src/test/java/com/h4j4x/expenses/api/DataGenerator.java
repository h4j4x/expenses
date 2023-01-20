package com.h4j4x.expenses.api;

import com.github.javafaker.Faker;
import java.util.Locale;
import javax.inject.Singleton;

@Singleton
public class DataGenerator {
    private final Faker faker;

    public DataGenerator() {
        faker = new Faker(new Locale("en-US"));
    }

    public String genUserName() {
        return faker.name().name();
    }

    public String getUserFirstName() {
        return faker.name().firstName();
    }

    public String genUserEmail() {
        return faker.name().username() + "@mail.com";
    }

    public String genUserPassword() {
        return faker.random().hex(8);
    }

    public String genProductName() {
        return faker.commerce().productName();
    }

    public int genRandomNumber(int min, int max) {
        return faker.number().numberBetween(min, max);
    }

    public Long genRandomLong() {
        return Math.abs(faker.random().nextLong());
    }

    public double genRandomDouble() {
        return Math.abs(faker.random().nextDouble());
    }

    public String genRandomNotes(int minCount, int maxCount) {
        return faker.lorem().characters(minCount, maxCount);
    }
}
