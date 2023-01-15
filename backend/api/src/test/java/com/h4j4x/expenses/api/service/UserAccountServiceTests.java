package com.h4j4x.expenses.api.service;

import com.h4j4x.expenses.api.DataGenerator;
import com.h4j4x.expenses.api.TestConstants;
import com.h4j4x.expenses.api.domain.UserAccount;
import com.h4j4x.expenses.api.domain.UserEntity;
import com.h4j4x.expenses.api.domain.UserTransaction;
import com.h4j4x.expenses.api.model.PageData;
import com.h4j4x.expenses.api.model.UserAccountDTO;
import com.h4j4x.expenses.api.repository.UserAccountRepository;
import com.h4j4x.expenses.api.repository.UserTransactionRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class UserAccountServiceTests {
    @InjectMock
    UserAccountRepository accountRepo;

    @InjectMock
    UserTransactionRepository transactionRepo;

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
    void whenCreateAccount_WithBalance_Then_ShouldCreateUserAccountAndInitialTransaction() {
        var user = new UserEntity(dataGen.genUserName(), dataGen.genUserEmail(), dataGen.genUserPassword());
        var account = new UserAccount(user, dataGen.genProductName());
        account.setId(dataGen.genRandomLong());
        Mockito
            .when(accountRepo.countByUserAndName(user, account.getName()))
            .thenReturn(Uni.createFrom().item(0L));
        Mockito
            .when(accountRepo.save(Mockito.any()))
            .thenReturn(Uni.createFrom().item(account));

        var transaction = new UserTransaction(
            account, UserAccountService.TRANSACTION_INITIAL_BALANCE_NOTES, dataGen.getRandomDouble());
        transaction.setId(dataGen.genRandomLong());
        Mockito
            .when(transactionRepo.save(Mockito.any()))
            .thenReturn(Uni.createFrom().item(transaction));

        var uni = accountService.addAccount(user, new UserAccountDTO(account.getName(), transaction.getAmount()));
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        var userAccount = subscriber
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();
        assertNotNull(userAccount);
        assertEquals(account.getKey(), userAccount.getKey());
        assertEquals(account.getName(), userAccount.getName());
        assertEquals(account.getBalance(), userAccount.getBalance());

        Mockito.verify(accountRepo).countByUserAndName(user, account.getName());
        Mockito.verify(accountRepo).save(account);
        Mockito.verifyNoMoreInteractions(accountRepo);
        Mockito.verify(transactionRepo).save(transaction);
        Mockito.verifyNoMoreInteractions(transactionRepo);
    }

    @Test
    void whenGetAccounts_Then_ShouldGetUserAccounts() {
        var user = new UserEntity(dataGen.genUserName(), dataGen.genUserEmail(), dataGen.genUserPassword());
        user.setId(dataGen.genRandomLong());
        var itemsCount = dataGen.genRandomNumber(5, 10);
        List<UserAccount> items = new ArrayList<>(itemsCount);
        for (int i = 0; i < itemsCount; i++) {
            var item = new UserAccount(user, dataGen.genProductName());
            item.setId(dataGen.genRandomLong());
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

        Mockito.verify(accountRepo).findAllByUser(user);
        Mockito.verifyNoMoreInteractions(accountRepo);
    }

    @Test
    void whenGetPagedAccounts_Then_ShouldGetUserPagedAccounts() {
        var user = new UserEntity(dataGen.genUserName(), dataGen.genUserEmail(), dataGen.genUserPassword());
        user.setId(dataGen.genRandomLong());
        var itemsCount = dataGen.genRandomNumber(5, 10);
        List<UserAccount> items = new ArrayList<>(itemsCount);
        for (int i = 0; i < itemsCount; i++) {
            var item = new UserAccount(user, dataGen.genProductName());
            item.setId(dataGen.genRandomLong());
            items.add(item);
        }
        var pageIndex = 0;
        var pageSize = 2;
        Mockito
            .when(accountRepo.findPageByUser(user, pageIndex, pageSize))
            .thenReturn(Uni.createFrom()
                .item(PageData.create(items.subList(pageIndex, pageSize), pageIndex, pageSize, itemsCount)));

        var uni = accountService.getAccountsPaged(user, pageIndex, pageSize);
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        var page = subscriber
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();
        assertNotNull(page);
        assertEquals(pageIndex, page.pageIndex());
        assertEquals(pageSize, page.pageSize());
        assertEquals(itemsCount, page.totalCount());
        assertEquals(pageSize, page.list().size());
        page.list().forEach(item -> assertTrue(items.contains(item)));

        Mockito.verify(accountRepo).findPageByUser(user, pageIndex, pageSize);
        Mockito.verifyNoMoreInteractions(accountRepo);
    }

    @Test
    void whenEditAccount_WithInvalidUser_Then_ShouldThrow404() {
        var user = new UserEntity(dataGen.genUserName(), dataGen.genUserEmail(), dataGen.genUserPassword());
        user.setId(dataGen.genRandomLong());
        var otherUser = new UserEntity(dataGen.genUserName(), dataGen.genUserEmail(), dataGen.genUserPassword());
        otherUser.setId(dataGen.genRandomLong());
        var account = new UserAccount(otherUser, dataGen.genProductName());
        account.setId(dataGen.genRandomLong());

        var uni = accountService.editAccount(user, account.getKey(), new UserAccountDTO(dataGen.genProductName()));
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        subscriber
            .awaitFailure(TestConstants.UNI_DURATION)
            .assertFailedWith(NotFoundException.class, UserAccountService.ACCOUNT_NOT_FOUND_MESSAGE);
        Mockito.verifyNoMoreInteractions(accountRepo);
    }

    @Test
    void whenEditAccount_WithInvalidAccount_Then_ShouldThrow404() {
        var user = new UserEntity(dataGen.genUserName(), dataGen.genUserEmail(), dataGen.genUserPassword());
        user.setId(dataGen.genRandomLong());
        var account = new UserAccount(user, dataGen.genProductName());
        account.setId(dataGen.genRandomLong());
        Mockito
            .when(accountRepo.findByUserAndId(user, account.getId()))
            .thenReturn(Uni.createFrom().optional(Optional.empty()));

        var uni = accountService.editAccount(user, account.getKey(), new UserAccountDTO(dataGen.genProductName()));
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        subscriber
            .awaitFailure(TestConstants.UNI_DURATION)
            .assertFailedWith(NotFoundException.class, UserAccountService.ACCOUNT_NOT_FOUND_MESSAGE);
        Mockito.verify(accountRepo).findByUserAndId(user, account.getId());
        Mockito.verifyNoMoreInteractions(accountRepo);
    }

    @Test
    void whenEditAccount_WithExistentName_Then_ShouldThrow400() {
        var user = new UserEntity(dataGen.genUserName(), dataGen.genUserEmail(), dataGen.genUserPassword());
        user.setId(dataGen.genRandomLong());
        var account = new UserAccount(user, dataGen.genProductName());
        account.setId(dataGen.genRandomLong());
        UserAccountDTO accountDTO = new UserAccountDTO(dataGen.genProductName());
        Mockito
            .when(accountRepo.findByUserAndId(user, account.getId()))
            .thenReturn(Uni.createFrom().item(account));
        Mockito
            .when(accountRepo.countByUserAndNameAndNotId(user, accountDTO.getName(), account.getId()))
            .thenReturn(Uni.createFrom().item(1L));

        var uni = accountService.editAccount(user, account.getKey(), accountDTO);
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        subscriber
            .awaitFailure(TestConstants.UNI_DURATION)
            .assertFailedWith(BadRequestException.class, UserAccountService.ACCOUNT_NAME_EXISTS_MESSAGE);
        Mockito.verify(accountRepo).findByUserAndId(user, account.getId());
        Mockito.verify(accountRepo).countByUserAndNameAndNotId(user, accountDTO.getName(), account.getId());
        Mockito.verifyNoMoreInteractions(accountRepo);
    }

    @Test
    void whenEditAccount_WithNewName_Then_ShouldEditUserAccount() {
        var user = new UserEntity(dataGen.genUserName(), dataGen.genUserEmail(), dataGen.genUserPassword());
        user.setId(dataGen.genRandomLong());
        var account = new UserAccount(user, dataGen.genProductName());
        account.setId(dataGen.genRandomLong());
        var edited = new UserAccount(user, dataGen.genProductName());
        edited.setId(account.getId());
        Mockito
            .when(accountRepo.findByUserAndId(user, account.getId()))
            .thenReturn(Uni.createFrom().item(account));
        Mockito
            .when(accountRepo.countByUserAndNameAndNotId(user, edited.getName(), account.getId()))
            .thenReturn(Uni.createFrom().item(0L));
        Mockito
            .when(accountRepo.save(edited))
            .thenReturn(Uni.createFrom().item(edited));

        var uni = accountService.editAccount(user, account.getKey(), UserAccountDTO.fromAccount(edited));
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        var userAccount = subscriber
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();
        assertNotNull(userAccount);
        assertEquals(edited.getKey(), userAccount.getKey());
        assertEquals(edited.getName(), userAccount.getName());
        assertEquals(edited.getBalance(), userAccount.getBalance());

        Mockito.verify(accountRepo).findByUserAndId(user, account.getId());
        Mockito.verify(accountRepo).countByUserAndNameAndNotId(user, edited.getName(), account.getId());
        Mockito.verify(accountRepo).save(edited);
        Mockito.verifyNoMoreInteractions(accountRepo);
    }

    @Test
    void whenEditAccount_WithNewBalance_Then_ShouldEditUserAccountAndCreateTransaction() {
        var user = new UserEntity(dataGen.genUserName(), dataGen.genUserEmail(), dataGen.genUserPassword());
        user.setId(dataGen.genRandomLong());
        var account = new UserAccount(user, dataGen.genProductName());
        account.setId(dataGen.genRandomLong());
        var edited = new UserAccount(user, dataGen.genProductName(), dataGen.getRandomDouble());
        edited.setId(account.getId());
        Mockito
            .when(accountRepo.findByUserAndId(user, account.getId()))
            .thenReturn(Uni.createFrom().item(account));
        Mockito
            .when(accountRepo.countByUserAndNameAndNotId(user, edited.getName(), account.getId()))
            .thenReturn(Uni.createFrom().item(0L));
        Mockito
            .when(accountRepo.save(edited))
            .thenReturn(Uni.createFrom().item(edited));

        var transaction = new UserTransaction(
            account, UserAccountService.TRANSACTION_ADJUSTED_BALANCE_NOTES, edited.getBalance());
        transaction.setId(dataGen.genRandomLong());
        Mockito
            .when(transactionRepo.save(Mockito.any()))
            .thenReturn(Uni.createFrom().item(transaction));

        var uni = accountService.editAccount(user, account.getKey(), UserAccountDTO.fromAccount(edited));
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        var userAccount = subscriber
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();
        assertNotNull(userAccount);
        assertEquals(edited.getKey(), userAccount.getKey());
        assertEquals(edited.getName(), userAccount.getName());
        assertEquals(edited.getBalance(), userAccount.getBalance());

        Mockito.verify(accountRepo).findByUserAndId(user, account.getId());
        Mockito.verify(accountRepo).countByUserAndNameAndNotId(user, edited.getName(), account.getId());
        Mockito.verify(accountRepo).save(edited);
        Mockito.verifyNoMoreInteractions(accountRepo);
        Mockito.verify(transactionRepo).save(transaction);
        Mockito.verifyNoMoreInteractions(transactionRepo);
    }
}
