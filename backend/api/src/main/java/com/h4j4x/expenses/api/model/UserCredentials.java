package com.h4j4x.expenses.api.model;

public class UserCredentials {
    public UserCredentials() {
    }

    public UserCredentials(String email, String password) {
        this.email = email;
        this.password = password;
    }

    private String email;

    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
