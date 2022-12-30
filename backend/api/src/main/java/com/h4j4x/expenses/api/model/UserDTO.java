package com.h4j4x.expenses.api.model;

import java.security.Principal;

public class UserDTO extends UserCredentials implements Principal {
    private String name;

    public UserDTO() {
    }

    public UserDTO(String name, String email, String password) {
        super(email, password);
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
