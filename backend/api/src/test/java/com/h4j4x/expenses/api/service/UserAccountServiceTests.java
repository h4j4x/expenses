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
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.*;

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
        account.setId(dataGen.genRandomLong());
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
        account.setId(dataGen.genRandomLong());
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
        assertEquals(account.getKey(), userAccount.getKey());
        assertEquals(account.getName(), userAccount.getName());

        Mockito.verify(accountRepo).countByUserAndName(user, account.getName());
        Mockito.verify(accountRepo).save(Mockito.any());
        Mockito.verifyNoMoreInteractions(accountRepo);
    }

    @Test
    void whenCreateAccounts_Then_ShouldGetUserAccounts() {
        var user = new UserEntity(dataGen.genUserName(), dataGen.genUserEmail(), dataGen.genUserPassword());
        user.setId(dataGen.genRandomLong());
        var itemsCount = dataGen.genRandomNumber(5, 10);
        List<UserAccount> items = new ArrayList<>(itemsCount);
        for (int i = 0; i < itemsCount; i++) {
            var account = new UserAccount(user, dataGen.genProductName());
            account.setId(dataGen.genRandomLong());
            Mockito
                .when(accountRepo.countByUserAndName(user, account.getName()))
                .thenReturn(Uni.createFrom().item(0L));
            Mockito
                .when(accountRepo.save(account))
                .thenReturn(Uni.createFrom().item(account));
            var item = accountService.addAccount(user, new UserAccountDTO(account.getName()))
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .awaitItem(TestConstants.UNI_DURATION)
                .getItem();
            items.add(item);
        }
        Mockito
            .when(accountRepo.findAllByUser(user))
            .thenReturn(Uni.createFrom().item(items));

        var uni = accountService.getAccounts(user);
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        var list = subscriber
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();
        assertNotNull(list);
        assertEquals(itemsCount, list.size());
        items.forEach(item -> assertTrue(list.contains(item)));

        Mockito.verify(accountRepo, Mockito.times(itemsCount)).countByUserAndName(Mockito.any(), Mockito.any());
        Mockito.verify(accountRepo, Mockito.times(itemsCount)).save(Mockito.any());
        Mockito.verify(accountRepo).findAllByUser(user);
        Mockito.verifyNoMoreInteractions(accountRepo);
    }
}
