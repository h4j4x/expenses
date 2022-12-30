package com.h4j4x.expenses.api.repository;

import com.h4j4x.expenses.api.domain.UserEntity;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

@ApplicationScoped
public class UserRepository implements PanacheRepository<UserEntity> {
    @Inject
    Validator validator;

    public Uni<UserEntity> save(UserEntity userEntity) {
        return Uni.createFrom().<UserEntity>emitter(emitter -> {
            Set<ConstraintViolation<UserEntity>> violations = validator.validate(userEntity);
            if (!violations.isEmpty()) {
                emitter.fail(new ConstraintViolationException(violations));
            }
            emitter.complete(userEntity);
        }).flatMap(this::persist);
    }

    public Uni<UserEntity> findByEmail(String email) {
        return find("email", email).firstResult();
    }
}
