package com.h4j4x.expenses.api.model;

import java.util.Objects;

public class UserCredentials {
    public UserCredentials(String email, String password) {
        this.email = email;
        this.password = password;
    }

    private final String email;

    private final String password;

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserCredentials that = (UserCredentials) o;
        return Objects.equals(email, that.email) && Objects.equals(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, password);
    }
}
