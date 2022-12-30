package com.h4j4x.expenses.api.model;

import com.h4j4x.expenses.api.domain.UserEntity;
import java.util.Objects;

public class UserDTO extends UserCredentials {
    private final String name;

    public UserDTO(String name, String email, String password) {
        super(email, password);
        this.name = name;
    }

    public static UserDTO fromEntity(UserEntity userEntity) {
        if (userEntity != null) {
            return new UserDTO(userEntity.getName(), userEntity.getEmail(), null);
        }
        return null;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        UserDTO userDTO = (UserDTO) o;
        return Objects.equals(name, userDTO.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name);
    }
}
