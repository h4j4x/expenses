package com.h4j4x.expenses.api.model;

import com.h4j4x.expenses.api.domain.UserCategory;
import java.util.Objects;

public class UserCategoryDTO {
    private String key;

    private String name;

    public UserCategoryDTO() {
    }

    public UserCategoryDTO(String name) {
        this.name = name;
    }

    public static UserCategoryDTO fromCategory(UserCategory category) {
        if (category != null) {
            UserCategoryDTO dto = new UserCategoryDTO(category.getName());
            dto.setKey(category.getKey());
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserCategoryDTO that = (UserCategoryDTO) o;
        if (Objects.equals(key, that.key)) return true;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, name);
    }
}
