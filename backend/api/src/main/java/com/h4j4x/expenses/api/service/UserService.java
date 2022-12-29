package com.h4j4x.expenses.api.service;

import com.h4j4x.expenses.api.domain.UserEntity;
import com.h4j4x.expenses.api.repository.UserRepository;
import io.smallrye.mutiny.Uni;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserService {
    private final UserRepository userRepo;

    public UserService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public Uni<UserEntity> findUserByEmail(String email) {
        return userRepo.findByEmail(email);
    }

    public Uni<UserEntity> findUserByEmailAndPassword(String email, String password) {
        return userRepo.findByEmail(email)
            .onItem().transform(userEntity -> {
                // todo: password hasher
                if (password.equals(userEntity.getPassword())) {
                    return userEntity;
                }
                return null;
            });
    }
}
