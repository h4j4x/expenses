package com.h4j4x.expenses.api.domain;

import java.security.Principal;
import java.time.OffsetDateTime;
import java.util.Objects;
import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "users")
public class UserEntity implements Principal {
    @Id
    @GeneratedValue
    private Long id;

    @NotBlank(message = "User name may not be blank")
    @Column(nullable = false)
    private String name;

    @Email(message = "User email must be valid")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "User password may not be blank")
    @Column(nullable = false)
    private String password;

    @Size(max = 100)
    @Column(length = 100)
    private String salt;

    @NotNull(message = "User created at is required")
    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    public UserEntity() {
        createdAt = OffsetDateTime.now();
    }

    public UserEntity(String name, String email, String password) {
        this();
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserEntity that = (UserEntity) o;
        if (Objects.equals(id, that.id)) return true;
        return email.equals(that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }
}
