package com.h4j4x.expenses.api.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

public abstract class BaseRepository<Entity> implements PanacheRepository<Entity> {
    protected Uni<Entity> save(Entity entity, Validator validator) {
        return Uni.createFrom().<Entity>emitter(emitter -> {
            Set<ConstraintViolation<Entity>> violations = validator.validate(entity);
            if (!violations.isEmpty()) {
                emitter.fail(new ConstraintViolationException(violations));
            }
            emitter.complete(entity);
        }).flatMap(this::persist);
    }
}
