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
        return userRepo.countByEmail(user.getEmail())
            .onItem().transform(count -> {
                if (count > 0) {
                    return null;
                }
                return user;
            })
            .onItem().ifNull().failWith(new BadRequestException(USER_EMAIL_EXISTS_MESSAGE))
            .onItem().ifNotNull().transformToUni(this::createUserEntity);
    }

    private Uni<UserEntity> createUserEntity(UserDTO user) {
        return Uni.createFrom().<UserEntity>emitter(emitter -> {
            try {
                var salt = stringHasher.salt();
                var password = stringHasher.hash(user.getPassword(), salt);
                var entity = new UserEntity(user.getName(), user.getEmail(), password);
                entity.setSalt(salt);
                emitter.complete(entity);
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

    public Uni<UserEntity> editUser(UserEntity entity, UserDTO user) {
        return Uni.createFrom().<UserEntity>emitter(emitter -> {
                try {
                    setUserData(entity, user);
                    emitter.complete(entity);
                } catch (GeneralSecurityException e) {
                    emitter.fail(e);
                }
            })
            .chain(userEntity -> userRepo
                .countByEmailAndNotId(userEntity.getEmail(), userEntity.getId())
                .onItem().transform(count -> {
                    if (count > 0) {
                        return null;
                    }
                    return userEntity;
                })
                .onItem().ifNull().failWith(new BadRequestException(USER_EMAIL_EXISTS_MESSAGE))
                .onItem().ifNotNull().transformToUni(userRepo::save));
    }

    private void setUserData(UserEntity entity, UserDTO user) throws GeneralSecurityException {
        if (user.getName() != null) {
            entity.setName(user.getName());
        }
        if (user.getEmail() != null) {
            entity.setEmail(user.getEmail());
        }
        if (user.getPassword() != null) {
            entity.setPassword(stringHasher.hash(user.getPassword(), entity.getSalt()));
        }
    }
}
