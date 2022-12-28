package com.h4j4x.expenses.api.model;

import java.security.Principal;

public class User implements Principal {
    private String name;

    private String email;

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
