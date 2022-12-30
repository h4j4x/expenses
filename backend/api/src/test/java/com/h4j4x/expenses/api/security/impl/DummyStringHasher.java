package com.h4j4x.expenses.api.security.impl;

import com.h4j4x.expenses.api.security.StringHasher;
import java.util.Objects;

public class DummyStringHasher implements StringHasher {
    @Override
    public String salt() {
        return "salt";
    }

    @Override
    public String hash(String string, String salt) {
        return string;
    }

    @Override
    public boolean match(String string, String salt, String hash) {
        return Objects.equals(string, hash);
    }
}
