package com.h4j4x.expenses.api.repository;

import com.h4j4x.expenses.api.DataGenerator;
import com.h4j4x.expenses.api.TestConstants;
import com.h4j4x.expenses.api.domain.UserAccount;
import com.h4j4x.expenses.api.domain.UserEntity;
import com.h4j4x.expenses.api.domain.UserTransaction;
import com.h4j4x.expenses.api.model.AccountType;
import com.h4j4x.expenses.api.model.TransactionCreationWay;
import com.h4j4x.expenses.api.model.TransactionStatus;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
public class UserTransactionRepositoryTests {
    @Inject
    UserTransactionRepository transactionRepo;

    @Inject
    UserAccountRepository accountRepo;

    @Inject
    UserRepository userRepo;

    @Inject
    DataGenerator dataGen;

    @Test
    void whenCreateTransaction_Invalid_Then_ShouldThrowError() {
        var transaction = new UserTransaction();
        var uni = transactionRepo.save(transaction);
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        subscriber
            .awaitFailure(TestConstants.UNI_DURATION)
            .assertFailedWith(ConstraintViolationException.class);
    }

    @Test
    void whenCreateTransaction_WithoutAccount_Then_ShouldThrowError() {
        var transaction = new UserTransaction(dataGen.genProductName(), dataGen.getRandomDouble());
        var uni = transactionRepo.save(transaction);
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        subscriber
            .awaitFailure(TestConstants.UNI_DURATION)
            .assertFailedWith(ConstraintViolationException.class);
    }

    @Test
    void whenCreateTransaction_Then_ShouldAssignId() {
        var account = createAccount();
        var transaction = new UserTransaction(account, dataGen.genProductName(), dataGen.getRandomDouble());
        transaction.setCreationWay(TransactionCreationWay.MANUAL);
        transaction.setStatus(TransactionStatus.CONFIRMED);
        var uni = transactionRepo.save(transaction);
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        var userTransaction = subscriber
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();
        assertNotNull(userTransaction);
        assertNotNull(userTransaction.getId());
        assertEquals(transaction.getNotes(), userTransaction.getNotes());
        assertEquals(transaction.getAmount(), userTransaction.getAmount());
    }

    private UserAccount createAccount() {
        var entity = new UserEntity(dataGen.genUserName(), dataGen.genUserEmail(), dataGen.genUserPassword());
        var user = userRepo.save(entity)
            .subscribe().withSubscriber(UniAssertSubscriber.create())
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();
        var account = new UserAccount(user, dataGen.genProductName(), AccountType.MONEY, "usd");
        return accountRepo.save(account)
            .subscribe().withSubscriber(UniAssertSubscriber.create())
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();
    }
}
