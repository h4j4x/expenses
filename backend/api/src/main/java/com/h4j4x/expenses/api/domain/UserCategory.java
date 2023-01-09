package com.h4j4x.expenses.api.domain;

import com.h4j4x.expenses.common.util.KeyHandler;
import java.time.OffsetDateTime;
import java.util.Objects;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "user_categories", uniqueConstraints = {
    @UniqueConstraint(name = "uk_user_category_name", columnNames = {"user_id", "name"}),
})
public class UserCategory {
    private static final String KEY = "-UC-";

    @Id
    @GeneratedValue
    private Long id;

    @NotNull(message = "Category user is required")
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, updatable = false)
    private UserEntity user;

    @NotBlank(message = "Category name may not be blank")
    @Column(nullable = false)
    private String name;

    @NotNull(message = "Category created at is required")
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    public UserCategory() {
        createdAt = OffsetDateTime.now();
    }

    public UserCategory(String name) {
        this();
        this.name = name;
    }

    public UserCategory(UserEntity user, String name) {
        this(name);
        this.user = user;
    }

    public static Long parseUserId(String key) {
        return new KeyHandler(KEY).parsePrefix(key);
    }

    public static Long parseCategoryId(String key) {
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

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserCategory that = (UserCategory) o;
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
