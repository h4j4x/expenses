package com.h4j4x.expenses.api.repository;

import com.h4j4x.expenses.api.DataGen;
import com.h4j4x.expenses.api.TestConstants;
import com.h4j4x.expenses.api.domain.UserEntity;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class UserRepositoryTests extends DataGen {
    @Inject
    UserRepository userRepo;

    @Test
    void whenCreateUser_Invalid_Then_ShouldThrowError() {
        var user = new UserEntity(genUserName(), null, null);
        var uni = userRepo.save(user);
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        subscriber
            .awaitFailure(TestConstants.UNI_DURATION)
            .assertFailedWith(ConstraintViolationException.class);
    }

    @Test
    void whenCreateUser_InvalidEmail_Then_ShouldThrowError() {
        var user = new UserEntity(genUserName(), getUserFirstName(), genUserPassword());
        var uni = userRepo.save(user);
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        subscriber
            .awaitFailure(TestConstants.UNI_DURATION)
            .assertFailedWith(ConstraintViolationException.class);
    }

    @Test
    void whenCreateUser_Then_ShouldAssignId() {
        var user = new UserEntity(genUserName(), genUserEmail(), genUserPassword());
        var uni = userRepo.save(user);
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        var userEntity = subscriber
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();
        assertNotNull(userEntity);
        assertNotNull(userEntity.getId());
        assertEquals(user.getEmail(), userEntity.getEmail());
    }

    @Test
    void whenFindUser_ByEmail_Then_ShouldGetUser() {
        var user = new UserEntity(genUserName(), genUserEmail(), genUserPassword());
        var uni = userRepo.save(user);
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        subscriber
            .awaitItem(TestConstants.UNI_DURATION);

        uni = userRepo.findByEmail(user.getEmail());
        subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        UserEntity userEntity = subscriber
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();
        assertNotNull(userEntity);
        assertNotNull(userEntity.getId());
        assertEquals(user.getEmail(), userEntity.getEmail());
    }

    @Test
    void whenCountUser_ByEmail_Then_ShouldGetCount() {
        var user = new UserEntity(genUserName(), genUserEmail(), genUserPassword());
        var uni = userRepo.save(user);
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        subscriber
            .awaitItem(TestConstants.UNI_DURATION);

        var countUni = userRepo.countByEmail(user.getEmail());
        var countSubscriber = countUni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        var count = countSubscriber
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();
        assertEquals(1L, count);
    }

    @Test
    void whenCount_OtherUsersByEmail_Then_ShouldGetNothing() {
        var user = new UserEntity(genUserName(), genUserEmail(), genUserPassword());
        var uni = userRepo.save(user);
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        var entity = subscriber
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();

        var countUni = userRepo.countByEmailAndNotId(user.getEmail(), entity.getId());
        var countSubscriber = countUni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        var count = countSubscriber
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();
        assertEquals(0L, count);
    }

    @Test
    void whenCountUser_ByEmail_NonRegistered_Then_ShouldGetNothing() {
        var user = new UserEntity(genUserName(), genUserEmail(), genUserPassword());

        var uni = userRepo.countByEmail(user.getEmail());
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        var count = subscriber
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();
        assertEquals(0L, count);
    }

    @Test
    void whenFindUser_ByEmail_NonRegistered_Then_ShouldGetNothing() {
        var uni = userRepo.findByEmail(genUserEmail());
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        UserEntity userEntity = subscriber
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();
        assertNull(userEntity);
    }
}
