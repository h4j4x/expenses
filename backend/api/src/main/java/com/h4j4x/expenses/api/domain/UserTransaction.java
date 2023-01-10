package com.h4j4x.expenses.api.domain;

import com.h4j4x.expenses.api.model.TransactionCreationWay;
import com.h4j4x.expenses.api.model.TransactionStatus;
import com.h4j4x.expenses.common.util.KeyHandler;
import java.time.OffsetDateTime;
import java.util.Objects;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "user_transactions", indexes = {
    @Index(name = "idx_user_transactions_created_at", columnList = "created_at"),
    @Index(name = "idx_user_transactions_status", columnList = "status"),
})
public class UserTransaction {
    private static final String KEY = "-UT-";

    @Id
    @GeneratedValue
    private Long id;

    @NotNull(message = "Transaction user is required")
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, updatable = false)
    private UserEntity user;

    @NotNull(message = "Transaction account is required")
    @ManyToOne(optional = false)
    @JoinColumn(name = "account_id", referencedColumnName = "id", nullable = false, updatable = false)
    private UserAccount account;

    @ManyToOne
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    private UserCategory category;

    @Column(nullable = false, precision = 7, scale = 2, updatable = false)
    private double amount = .0;

    @NotBlank(message = "Transaction notes are required")
    @Column(nullable = false, updatable = false)
    private String notes;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Transaction status is required")
    @Column(nullable = false)
    private TransactionCreationWay creationWay;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Transaction status is required")
    @Column(nullable = false)
    private TransactionStatus status;

    @NotNull(message = "Transaction created at is required")
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    public UserTransaction() {
        createdAt = OffsetDateTime.now();
    }

    public UserTransaction(UserAccount account) {
        this();
        this.account = account;
        if (account != null) {
            this.user = account.getUser();
        }
    }

    public UserTransaction(UserAccount account, String notes, double amount) {
        this(account);
        this.notes = notes;
        this.amount = amount;
    }

    public UserTransaction(String notes, double amount) {
        this.notes = notes;
        this.amount = amount;
    }

    public static Long parseAccountId(String key) {
        return new KeyHandler(KEY).parsePrefix(key);
    }

    public static Long parseTransactionId(String key) {
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

    public UserAccount getAccount() {
        return account;
    }

    public void setAccount(UserAccount account) {
        this.account = account;
        if (account != null) {
            this.user = account.getUser();
        }
    }

    public UserCategory getCategory() {
        return category;
    }

    public void setCategory(UserCategory category) {
        this.category = category;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public TransactionCreationWay getCreationWay() {
        return creationWay;
    }

    public void setCreationWay(TransactionCreationWay creationWay) {
        this.creationWay = creationWay;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserTransaction that = (UserTransaction) o;
        if (Objects.equals(id, that.id)) return true;
        return account.equals(that.account) && notes.equals(that.notes) && amount == that.getAmount();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, notes, amount);
    }

    public String getKey() {
        if (account != null && id != null) {
            return new KeyHandler(KEY).createKey(account.getId(), id);
        }
        return null;
    }
}
