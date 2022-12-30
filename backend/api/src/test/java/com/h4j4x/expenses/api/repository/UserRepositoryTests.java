package com.h4j4x.expenses.api.repository;

import com.h4j4x.expenses.api.domain.UserEntity;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import java.time.Duration;
import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class UserRepositoryTests {
    @Inject
    UserRepository userRepo;

    @Test
    void whenCreateUser_Invalid_Then_ShouldThrowError() {
        var user = new UserEntity("name", null, null);
        var uni = userRepo.save(user);
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        subscriber
            .awaitFailure(Duration.ofMillis(100))
            .assertFailedWith(ConstraintViolationException.class);
    }

    @Test
    void whenCreateUser_InvalidEmail_Then_ShouldThrowError() {
        var user = new UserEntity("name", "email", "password");
        var uni = userRepo.save(user);
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        subscriber
            .awaitFailure(Duration.ofMillis(100))
            .assertFailedWith(ConstraintViolationException.class);
    }

    @Test
    void whenCreateUser_Then_ShouldAssignId() {
        var user = new UserEntity("name", "email@mail.com", "password");
        var uni = userRepo.save(user);
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        var userEntity = subscriber
            .awaitItem(Duration.ofMillis(100))
            .getItem();
        assertNotNull(userEntity);
        assertNotNull(userEntity.getId());
        assertEquals(user.getEmail(), userEntity.getEmail());
    }

    @Test
    void whenFindUserByEmail_Then_ShouldGetUser() {
        var user = new UserEntity("name", "email-1@mail.com", "password");
        var uni = userRepo.save(user);
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        subscriber
            .awaitItem(Duration.ofMillis(100));

        uni = userRepo.findByEmail(user.getEmail());
        subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        UserEntity userEntity = subscriber
            .awaitItem(Duration.ofMillis(100))
            .getItem();
        assertNotNull(userEntity);
        assertNotNull(userEntity.getId());
        assertEquals(user.getEmail(), userEntity.getEmail());
    }

    @Test
    void whenFindUserByEmail_NonRegistered_Then_ShouldGetNothing() {
        var uni = userRepo.findByEmail("non-email@mail.com");
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        UserEntity userEntity = subscriber
            .awaitItem(Duration.ofMillis(100))
            .getItem();
        assertNull(userEntity);
    }
}
