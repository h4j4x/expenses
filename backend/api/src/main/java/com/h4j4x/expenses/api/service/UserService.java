package com.h4j4x.expenses.api.service;

import com.h4j4x.expenses.api.domain.UserEntity;
import com.h4j4x.expenses.api.model.UserCredentials;
import com.h4j4x.expenses.api.model.UserDTO;
import com.h4j4x.expenses.api.repository.UserRepository;
import com.h4j4x.expenses.api.security.StringHasher;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import java.security.GeneralSecurityException;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.BadRequestException;

@ApplicationScoped
public class UserService {
    public static final String USER_EMAIL_EXISTS_MESSAGE = "User email already registered";

    private final UserRepository userRepo;
    private final StringHasher stringHasher;

    public UserService(UserRepository userRepo, StringHasher stringHasher) {
        this.userRepo = userRepo;
        this.stringHasher = stringHasher;
    }

    public Uni<UserEntity> createUser(UserDTO user) {
        return userRepo.findByEmail(user.getEmail())
            .onItem().ifNotNull().failWith(new BadRequestException(USER_EMAIL_EXISTS_MESSAGE))
            .onItem().ifNull().switchTo(() -> createUserEntity(user));
    }

    private Uni<UserEntity> createUserEntity(UserDTO userDTO) {
        return Uni.createFrom().<UserEntity>emitter(emitter -> {
            try {
                var salt = stringHasher.salt();
                var password = stringHasher.hash(userDTO.getPassword(), salt);
                var userEntity = new UserEntity(userDTO.getName(), userDTO.getEmail(), password);
                userEntity.setSalt(salt);
                emitter.complete(userEntity);
            } catch (GeneralSecurityException e) {
                emitter.fail(e);
            }
        }).flatMap(userRepo::save);
    }

    public Uni<UserEntity> findUserByEmail(String email) {
        return userRepo.findByEmail(email);
    }

    public Uni<UserEntity> findUserByEmailAndPassword(UserCredentials credentials) {
        return userRepo.findByEmail(credentials.getEmail())
            .onItem().ifNotNull().transform(Unchecked.function(userEntity -> {
                if (stringHasher.match(credentials.getPassword(), userEntity.getSalt(), userEntity.getPassword())) {
                    return userEntity;
                }
                return null;
            }));
    }
}
