package com.h4j4x.expenses.api.domain;

import java.security.Principal;
import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

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

    public UserEntity() {
    }

    public UserEntity(String name, String email, String password) {
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
}
