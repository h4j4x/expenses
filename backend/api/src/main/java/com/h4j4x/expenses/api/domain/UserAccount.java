package com.h4j4x.expenses.api.domain;

import com.h4j4x.expenses.api.model.AccountType;
import com.h4j4x.expenses.common.util.KeyHandler;
import java.time.OffsetDateTime;
import java.util.Objects;
import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "user_accounts", uniqueConstraints = {
    @UniqueConstraint(name = "uk_user_account_name", columnNames = {"user_id", "name"})
})
public class UserAccount {
    private static final String KEY = "-UA-";

    @Id
    @GeneratedValue
    private Long id;

    @NotNull(message = "Account user is required")
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, updatable = false)
    private UserEntity user;

    @NotBlank(message = "Account name may not be blank")
    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Account type is required")
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    @NotBlank(message = "Account currency may not be blank")
    @Column(nullable = false)
    private String currency;

    @Min(value = 0, message = "Account balance cannot be negative")
    @Column(nullable = false, precision = 7, scale = 2)
    private double balance = .0;

    @NotNull(message = "Account balance updated at is required")
    @Column(name = "balance_updated_at")
    private OffsetDateTime balanceUpdatedAt;

    public UserAccount() {
        balanceUpdatedAt = OffsetDateTime.now();
    }

    public UserAccount(String name) {
        this();
        this.name = name;
    }

    public UserAccount(UserEntity user, String name) {
        this(name);
        this.user = user;
    }

    public UserAccount(UserEntity user, String name, AccountType accountType, String currency) {
        this(user, name);
        this.accountType = accountType;
        this.currency = currency;
    }

    public static Long parseUserId(String key) {
        return new KeyHandler(KEY).parsePrefix(key);
    }

    public static Long parseAccountId(String key) {
        return new KeyHandler(KEY).parseSuffix(key);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public OffsetDateTime getBalanceUpdatedAt() {
        return balanceUpdatedAt;
    }

    public void setBalanceUpdatedAt(OffsetDateTime balanceUpdatedAt) {
        this.balanceUpdatedAt = balanceUpdatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserAccount that = (UserAccount) o;
        if (Objects.equals(id, that.id)) return true;
        return user.equals(that.user) && name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    public String getKey() {
        if (user != null && id != null) {
            return new KeyHandler(KEY).createKey(user.getId(), id);
        }
        return null;
    }
}
