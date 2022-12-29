package com.h4j4x.expenses.api.repository;

import com.h4j4x.expenses.api.domain.UserEntity;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserRepository implements PanacheRepository<UserEntity> {
    public Uni<UserEntity> findByEmail(String email) {
        return find("email", email).firstResult();
    }
}
