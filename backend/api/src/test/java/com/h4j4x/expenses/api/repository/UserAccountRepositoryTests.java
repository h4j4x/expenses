package com.h4j4x.expenses.api.repository;

import com.h4j4x.expenses.api.DataGenerator;
import com.h4j4x.expenses.api.TestConstants;
import com.h4j4x.expenses.api.domain.UserAccount;
import com.h4j4x.expenses.api.domain.UserEntity;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import java.util.ArrayList;
import java.util.List;
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
    void whenFindAccount_ByUserAndName_Then_ShouldGetUserAccount() {
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
    void whenFindAccount_ByUserAndId_Then_ShouldGetUserAccount() {
        var user = createUser();
        var account = new UserAccount(user, dataGen.genProductName());
        var uni = accountRepo.save(account);
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        account = subscriber
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();

        uni = accountRepo.findByUserAndId(user, account.getId());
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
    void whenCountAccounts_ByUser_Then_ShouldGetCount() {
        var user = createUser();
        var itemsCount = dataGen.genRandomNumber(1, 5);
        for (int i = 0; i < itemsCount; i++) {
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
        assertEquals(itemsCount, count);
    }

    @Test
    void whenFindAccounts_ByUser_Then_ShouldGetData() {
        var user = createUser();
        var itemsCount = dataGen.genRandomNumber(5, 10);
        List<UserAccount> items = new ArrayList<>(itemsCount);
        for (int i = 0; i < itemsCount; i++) {
            var account = new UserAccount(user, dataGen.genProductName());
            var item = accountRepo.save(account)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .awaitItem(TestConstants.UNI_DURATION)
                .getItem();
            items.add(item);
        }

        var findUni = accountRepo.findAllByUser(user);
        var findSubscriber = findUni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        var list = findSubscriber
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();
        assertEquals(itemsCount, list.size());
        items.forEach(item -> assertTrue(list.contains(item)));
    }

    @Test
    void whenCountAccounts_ByUserAndNameAndSameId_Then_ShouldGetNothing() {
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
    void whenCountAccounts_ByUserAndNameAndOtherId_Then_ShouldGetOne() {
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

    @Test
    void whenCountAccounts_ByUserAndName_Then_ShouldGetOne() {
        var user = createUser();
        var account = new UserAccount(user, dataGen.genProductName());
        var userAccount = accountRepo.save(account)
            .subscribe().withSubscriber(UniAssertSubscriber.create())
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();

        var countUni = accountRepo.countByUserAndName(user, userAccount.getName());
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
