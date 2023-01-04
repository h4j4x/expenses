package com.h4j4x.expenses.api.domain;

import java.time.OffsetDateTime;
import java.util.Objects;
import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Entity
@Table(name = "user_accounts", uniqueConstraints = {
    @UniqueConstraint(name = "uk_account_name", columnNames = {"user_id", "name"})
})
public class UserAccount {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, updatable = false)
    private UserEntity user;

    @NotBlank(message = "Account name may not be blank")
    @Column(nullable = false)
    private String name;

    @Min(0)
    @Column(nullable = false, precision = 7, scale = 2)
    private double balance = .0;

    @Column(name = "balance_updated_at")
    private OffsetDateTime balanceUpdatedAt;

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

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
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
}
