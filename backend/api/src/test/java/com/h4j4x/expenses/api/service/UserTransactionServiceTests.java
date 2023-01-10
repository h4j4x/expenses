package com.h4j4x.expenses.api.service;

import com.h4j4x.expenses.api.DataGenerator;
import com.h4j4x.expenses.api.TestConstants;
import com.h4j4x.expenses.api.domain.UserAccount;
import com.h4j4x.expenses.api.domain.UserEntity;
import com.h4j4x.expenses.api.domain.UserTransaction;
import com.h4j4x.expenses.api.model.UserTransactionDTO;
import com.h4j4x.expenses.api.repository.UserTransactionRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import javax.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
public class UserTransactionServiceTests {
    @InjectMock
    UserTransactionRepository transactionRepo;

    @Inject
    UserTransactionService transactionService;

    @Inject
    DataGenerator dataGen;

    @Test
    void whenCreateTransaction_Then_ShouldCreateUserTransaction() {
        var user = new UserEntity(dataGen.genUserName(), dataGen.genUserEmail(), dataGen.genUserPassword());
        var account = new UserAccount(user, dataGen.genProductName());
        account.setId(dataGen.genRandomLong());
        var transaction = new UserTransaction(account, dataGen.getRandomNotes(10, 200), dataGen.getRandomDouble());
        transaction.setId(dataGen.genRandomLong());
        Mockito
            .when(transactionRepo.save(Mockito.any()))
            .thenReturn(Uni.createFrom().item(transaction));


        var transactionDTO = new UserTransactionDTO(transaction.getNotes(), transaction.getAmount());
        var uni = transactionService.addTransaction(account, transactionDTO);
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        var userTransaction = subscriber
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();
        assertNotNull(userTransaction);
        assertEquals(transaction.getKey(), userTransaction.getKey());
        assertEquals(transaction.getNotes(), userTransaction.getNotes());
        assertEquals(transaction.getAmount(), userTransaction.getAmount());
        assertEquals(transaction.getStatus(), userTransaction.getStatus());
        assertEquals(transaction.getCreationWay(), userTransaction.getCreationWay());

        Mockito.verify(transactionRepo).save(Mockito.any());
        Mockito.verifyNoMoreInteractions(transactionRepo);
    }
}
