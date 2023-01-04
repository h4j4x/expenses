package com.h4j4x.expenses.api.repository;

import com.h4j4x.expenses.api.DataGenerator;
import com.h4j4x.expenses.api.TestConstants;
import com.h4j4x.expenses.api.domain.UserAccount;
import com.h4j4x.expenses.api.domain.UserEntity;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class UserAccountRepositoryTests {
    @Inject
    UserAccountRepository accountRepo;

    @Inject
    UserRepository userRepo;

    @Inject
    DataGenerator dataGen;

    @Test
    void whenCreateAccount_Invalid_Then_ShouldThrowError() {
        var account = new UserAccount();
        var uni = accountRepo.save(account);
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        subscriber
            .awaitFailure(TestConstants.UNI_DURATION)
            .assertFailedWith(ConstraintViolationException.class);
    }

    @Test
    void whenCreateAccount_WithoutUser_Then_ShouldThrowError() {
        var account = new UserAccount(dataGen.genProductName());
        var uni = accountRepo.save(account);
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        subscriber
            .awaitFailure(TestConstants.UNI_DURATION)
            .assertFailedWith(ConstraintViolationException.class);
    }

    @Test
    void whenCreateAccount_Then_ShouldAssignId() {
        var user = createUser();
        var account = new UserAccount(user, dataGen.genProductName());
        var uni = accountRepo.save(account);
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        var userAccount = subscriber
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();
        assertNotNull(userAccount);
        assertNotNull(userAccount.getId());
        assertEquals(account.getName(), userAccount.getName());
    }

    @Test
    void whenFindAccount_ByUserAndName_Then_ShouldGetUser() {
        var user = createUser();
        var account = new UserAccount(user, dataGen.genProductName());
        var uni = accountRepo.save(account);
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        subscriber
            .awaitItem(TestConstants.UNI_DURATION);

        uni = accountRepo.findByUserAndName(user, account.getName());
        subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        UserAccount userAccount = subscriber
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();
        assertNotNull(userAccount);
        assertNotNull(userAccount.getId());
        assertEquals(account.getName(), userAccount.getName());
    }

    @Test
    void whenFindAccount_ByUserAndInvalidName_Then_ShouldGetNothing() {
        var user = createUser();
        var account = new UserAccount(user, dataGen.genProductName());
        var uni = accountRepo.save(account);
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        subscriber
            .awaitItem(TestConstants.UNI_DURATION);

        uni = accountRepo.findByUserAndName(user, account.getName() + "-");
        subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        UserAccount userAccount = subscriber
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();
        assertNull(userAccount);
    }

    @Test
    void whenCountAccount_ByUserAndName_Then_ShouldGetCount() {
        var user = createUser();
        var items = dataGen.genRandomNumber(1, 5);
        for (int i = 0; i < items; i++) {
            var account = new UserAccount(user, dataGen.genProductName());
            accountRepo.save(account)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .awaitItem(TestConstants.UNI_DURATION);
        }

        var countUni = accountRepo.countByUser(user);
        var countSubscriber = countUni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        var count = countSubscriber
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();
        assertEquals(items, count);
    }

    @Test
    void whenCountAccount_ByUserAndNameAndSameId_Then_ShouldGetNothing() {
        var user = createUser();
        var account = new UserAccount(user, dataGen.genProductName());
        var userAccount = accountRepo.save(account)
            .subscribe().withSubscriber(UniAssertSubscriber.create())
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();

        var countUni = accountRepo.countByUserAndNameAndNotId(user, userAccount.getName(), userAccount.getId());
        var countSubscriber = countUni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        var count = countSubscriber
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();
        assertEquals(0L, count);
    }

    @Test
    void whenCountAccount_ByUserAndNameAndOtherId_Then_ShouldGetOne() {
        var user = createUser();
        var account = new UserAccount(user, dataGen.genProductName());
        var userAccount = accountRepo.save(account)
            .subscribe().withSubscriber(UniAssertSubscriber.create())
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();

        var countUni = accountRepo.countByUserAndNameAndNotId(user, userAccount.getName(), userAccount.getId() + 1);
        var countSubscriber = countUni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        var count = countSubscriber
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();
        assertEquals(1L, count);
    }

    private UserEntity createUser() {
        var entity = new UserEntity(dataGen.genUserName(), dataGen.genUserEmail(), dataGen.genUserPassword());
        return userRepo.save(entity)
            .subscribe().withSubscriber(UniAssertSubscriber.create())
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();
    }
}