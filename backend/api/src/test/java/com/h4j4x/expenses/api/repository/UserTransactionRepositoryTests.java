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
import io.smallrye.mutiny.helpers.test.AssertSubscriber;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
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
        var transaction = new UserTransaction(dataGen.genProductName(), dataGen.genRandomDouble());
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
        var transaction = new UserTransaction(account, dataGen.genProductName(), dataGen.genRandomDouble());
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

    @Test
    void whenFindTransactions_ByAccount_Then_ShouldGetStream() {
        var account = createAccount();
        var itemsCount = dataGen.genRandomNumber(5, 10);
        List<UserTransaction> items = new ArrayList<>(itemsCount);
        var status = TransactionStatus.CONFIRMED;
        var from = OffsetDateTime.now();
        var creationWay = TransactionCreationWay.SYSTEM;
        for (int i = 0; i < itemsCount; i++) {
            var transaction = new UserTransaction(account, dataGen.genRandomNotes(10, 200), dataGen.genRandomDouble());
            transaction.setStatus(status);
            transaction.setCreationWay(creationWay);
            var item = transactionRepo.save(transaction)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .awaitItem(TestConstants.UNI_DURATION)
                .getItem();
            items.add(item);
        }
        transactionRepo.flush()
            .subscribe().withSubscriber(UniAssertSubscriber.create())
            .awaitItem(TestConstants.UNI_DURATION);

        var multi = transactionRepo.findTransactions(account, from, status);
        var subscriber = multi.subscribe()
            .withSubscriber(AssertSubscriber.create(itemsCount));

        subscriber
            .awaitCompletion()
            .assertItems(items.toArray(new UserTransaction[0]));
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
