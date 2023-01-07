package com.h4j4x.expenses.api.model;

import com.h4j4x.expenses.api.domain.UserAccount;
import java.util.Objects;

public class UserAccountDTO {
    private String key;
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

    public static UserAccountDTO fromAccount(UserAccount account) {
        if (account != null) {
            UserAccountDTO dto = new UserAccountDTO(account.getName(), account.getBalance());
            dto.setKey(account.getKey());
            return dto;
        }
        return null;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserAccountDTO that = (UserAccountDTO) o;
        if (Objects.equals(key, that.key)) return true;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, name);
    }
}
