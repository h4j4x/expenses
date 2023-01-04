package com.h4j4x.expenses.api.repository;

import com.h4j4x.expenses.api.domain.UserEntity;
import io.smallrye.mutiny.Uni;
import javax.enterprise.context.ApplicationScoped;
import javax.validation.Validator;

@ApplicationScoped
public class UserRepository extends BaseRepository<UserEntity> {
    private final Validator validator;

    public UserRepository(Validator validator) {
        this.validator = validator;
    }

    public Uni<UserEntity> save(UserEntity user) {
        return super.save(user, validator);
    }

    public Uni<UserEntity> findByEmail(String email) {
        return find("email", email).firstResult();
    }

    public Uni<Long> countByEmail(String email) {
        return count("email", email);
    }

    public Uni<Long> countByEmailAndNotId(String email, Long id) {
        return count("email = ?1 and id != ?2", email, id);
    }
}
