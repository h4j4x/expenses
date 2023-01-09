package com.h4j4x.expenses.api.model;

import com.h4j4x.expenses.api.domain.UserAccount;
import java.time.LocalDateTime;
import java.util.Objects;

public class UserAccountDTO {
    private String key;

    private String name;

    private AccountType accountType;

    private String currency;

    private double balance;

    private LocalDateTime balanceUpdatedAt;

    public UserAccountDTO() {
    }

    public UserAccountDTO(String name) {
        this.name = name;
    }

    public static UserAccountDTO fromAccount(UserAccount account) {
        if (account != null) {
            UserAccountDTO dto = new UserAccountDTO(account.getName());
            dto.setKey(account.getKey());
            dto.setAccountType(account.getAccountType());
            dto.setCurrency(account.getCurrency());
            dto.setBalance(account.getBalance());
            if (account.getBalanceUpdatedAt() != null) {
                dto.setBalanceUpdatedAt(account.getBalanceUpdatedAt().toLocalDateTime());
            }
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

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public LocalDateTime getBalanceUpdatedAt() {
        return balanceUpdatedAt;
    }

    public void setBalanceUpdatedAt(LocalDateTime balanceUpdatedAt) {
        this.balanceUpdatedAt = balanceUpdatedAt;
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
