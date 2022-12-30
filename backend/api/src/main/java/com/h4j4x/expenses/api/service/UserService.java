package com.h4j4x.expenses.api.service;

import com.h4j4x.expenses.api.domain.UserEntity;
import com.h4j4x.expenses.api.repository.UserRepository;
import io.smallrye.mutiny.Uni;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.BadRequestException;

@ApplicationScoped
public class UserService {
    public static final String USER_EMAIL_EXISTS_MESSAGE = "User email already registered";

    private final UserRepository userRepo;

    public UserService(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public Uni<UserEntity> createUser(String name, String email, String password) {
        return userRepo.findByEmail(email)
            .onItem().ifNotNull().failWith(new BadRequestException(USER_EMAIL_EXISTS_MESSAGE))
            .onItem().ifNull().switchTo(() -> createUserEntity(name, email, password));
    }

    private Uni<UserEntity> createUserEntity(String name, String email, String password) {
        // todo: password hasher
        var userEntity = new UserEntity(name, email, password);
        return userRepo.persist(userEntity);
    }

    public Uni<UserEntity> findUserByEmail(String email) {
        return userRepo.findByEmail(email);
    }

    public Uni<UserEntity> findUserByEmailAndPassword(String email, String password) {
        return userRepo.findByEmail(email)
            .onItem().ifNotNull().transform(userEntity -> {
                // todo: password hasher
                if (password.equals(userEntity.getPassword())) {
                    return userEntity;
                }
                return null;
            });
    }
}
