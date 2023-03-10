package com.h4j4x.expenses.api.repository;

import com.h4j4x.expenses.api.domain.UserCategory;
import com.h4j4x.expenses.api.domain.UserEntity;
import io.smallrye.mutiny.Uni;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.validation.Validator;

@ApplicationScoped
public class UserCategoryRepository extends BaseRepository<UserCategory> {
    private final Validator validator;

    public UserCategoryRepository(Validator validator) {
        this.validator = validator;
    }

    public Uni<UserCategory> save(UserCategory category) {
        return super.save(category, validator);
    }

    public Uni<UserCategory> findByUserAndName(UserEntity user, String name) {
        if (user != null) {
            return find("user_id = ?1 and name = ?2", user.getId(), name).firstResult();
        }
        return Uni.createFrom().optional(Optional.empty());
    }

    public Uni<UserCategory> findByUserAndId(UserEntity user, Long id) {
        if (user != null) {
            return find("user_id = ?1 and id = ?2", user.getId(), id).firstResult();
        }
        return Uni.createFrom().optional(Optional.empty());
    }

    public Uni<Long> countByUser(UserEntity user) {
        if (user != null) {
            return count("user_id", user.getId());
        }
        return Uni.createFrom().item(0L);
    }

    public Uni<List<UserCategory>> findAllByUser(UserEntity user) {
        if (user != null) {
            return find("user_id", user.getId()).list();
        }
        return Uni.createFrom().item(Collections.emptyList());
    }

    public Uni<Long> countByUserAndName(UserEntity user, String name) {
        if (user != null) {
            return count("user_id = ?1 and name = ?2", user.getId(), name);
        }
        return Uni.createFrom().item(0L);
    }

    public Uni<Long> countByUserAndNameAndNotId(UserEntity user, String name, Long id) {
        if (user != null) {
            return count("user_id = ?1 and name = ?2 and id != ?3", user.getId(), name, id);
        }
        return Uni.createFrom().item(0L);
    }
}
