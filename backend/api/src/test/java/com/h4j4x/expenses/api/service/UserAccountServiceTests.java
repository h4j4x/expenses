package com.h4j4x.expenses.api.service;

import com.h4j4x.expenses.api.DataGenerator;
import com.h4j4x.expenses.api.TestConstants;
import com.h4j4x.expenses.api.domain.UserAccount;
import com.h4j4x.expenses.api.domain.UserEntity;
import com.h4j4x.expenses.api.model.UserAccountDTO;
import com.h4j4x.expenses.api.repository.UserAccountRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
public class UserAccountServiceTests {
    @InjectMock
    UserAccountRepository accountRepo;

    @Inject
    UserAccountService accountService;

    @Inject
    DataGenerator dataGen;

    @Test
    void whenCreateAccount_WithRepeatedName_Then_ShouldThrowBadRequest() {
        var user = new UserEntity(dataGen.genUserName(), dataGen.genUserEmail(), dataGen.genUserPassword());
        var account = new UserAccount(user, dataGen.genProductName());
        account.setId(1L);
        Mockito
            .when(accountRepo.countByUserAndName(user, account.getName()))
            .thenReturn(Uni.createFrom().item(1L));

        var uni = accountService.addAccount(user, new UserAccountDTO(account.getName()));
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        subscriber
            .awaitFailure(TestConstants.UNI_DURATION)
            .assertFailedWith(BadRequestException.class, UserAccountService.ACCOUNT_NAME_EXISTS_MESSAGE);

        Mockito.verify(accountRepo).countByUserAndName(user, account.getName());
        Mockito.verifyNoMoreInteractions(accountRepo);
    }

    @Test
    void whenCreateAccount_Then_ShouldCreateUserAccount() {
        var user = new UserEntity(dataGen.genUserName(), dataGen.genUserEmail(), dataGen.genUserPassword());
        var account = new UserAccount(user, dataGen.genProductName());
        account.setId(1L);
        Mockito
            .when(accountRepo.countByUserAndName(user, account.getName()))
            .thenReturn(Uni.createFrom().item(0L));
        Mockito
            .when(accountRepo.save(Mockito.any()))
            .thenReturn(Uni.createFrom().item(account));

        var uni = accountService.addAccount(user, new UserAccountDTO(account.getName()));
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        var userAccount = subscriber
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();
        assertNotNull(userAccount);
        assertEquals(account.getName(), userAccount.getName());

        Mockito.verify(accountRepo).countByUserAndName(user, account.getName());
        Mockito.verify(accountRepo).save(Mockito.any());
        Mockito.verifyNoMoreInteractions(accountRepo);
    }
}
