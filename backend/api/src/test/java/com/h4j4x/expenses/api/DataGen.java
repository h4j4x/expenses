package com.h4j4x.expenses.api;

import com.github.javafaker.Faker;
import java.util.Locale;

public class DataGen {

    private final Faker faker = new Faker(new Locale("en-US"));

    protected String genUserName() {
        return faker.name().name();
    }

    protected String getUserFirstName() {
        return faker.name().firstName();
    }

    protected String genUserEmail() {
        return faker.name().username() + "@mail.com";
    }

    protected String genUserPassword() {
        return faker.random().hex(8);
    }

    protected String genRandomHex(int length) {
        return faker.random().hex(length);
    }

    protected String genProductName() {
        return faker.commerce().productName();
    }

    protected int genRandomNumber(int min, int max) {
        return faker.number().numberBetween(min, max);
    }
}
