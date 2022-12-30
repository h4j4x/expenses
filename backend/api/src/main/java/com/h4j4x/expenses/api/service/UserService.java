package com.h4j4x.expenses.api.service;

import com.h4j4x.expenses.api.domain.UserEntity;
import com.h4j4x.expenses.api.model.UserCredentials;
import com.h4j4x.expenses.api.model.UserDTO;
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

    public Uni<UserEntity> createUser(UserDTO user) {
        return userRepo.findByEmail(user.getEmail())
            .onItem().ifNotNull().failWith(new BadRequestException(USER_EMAIL_EXISTS_MESSAGE))
            .onItem().ifNull().switchTo(() -> createUserEntity(user));
    }

    private Uni<UserEntity> createUserEntity(UserDTO userDTO) {
        // todo: password hasher
        var userEntity = new UserEntity(userDTO.getName(), userDTO.getEmail(), userDTO.getPassword());
        return userRepo.save(userEntity);
    }

    public Uni<UserEntity> findUserByEmail(String email) {
        return userRepo.findByEmail(email);
    }

    public Uni<UserEntity> findUserByEmailAndPassword(UserCredentials credentials) {
        return userRepo.findByEmail(credentials.getEmail())
            .onItem().ifNotNull().transform(userEntity -> {
                // todo: password hasher
                if (credentials.getPassword().equals(userEntity.getPassword())) {
                    return userEntity;
                }
                return null;
            });
    }
}
