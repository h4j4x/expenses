package com.h4j4x.expenses.api.model;

import com.h4j4x.expenses.api.domain.UserAccount;

public class UserAccountDTO {
    private String name;

    private double balance;

    public UserAccountDTO() {
    }

    public UserAccountDTO(String name) {
        this.name = name;
    }

    public UserAccountDTO(String name, double balance) {
        this.name = name;
        this.balance = balance;
    }

    public static UserAccountDTO fromAccount(UserAccount userAccount) {
        if (userAccount != null) {
            return new UserAccountDTO(userAccount.getName(), userAccount.getBalance());
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }
}
