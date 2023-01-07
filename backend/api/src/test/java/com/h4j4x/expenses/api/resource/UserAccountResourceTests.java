package com.h4j4x.expenses.api.resource;

import com.h4j4x.expenses.api.DataGenerator;
import com.h4j4x.expenses.api.TestConstants;
import com.h4j4x.expenses.api.domain.UserAccount;
import com.h4j4x.expenses.api.domain.UserEntity;
import com.h4j4x.expenses.api.model.UserAccountDTO;
import com.h4j4x.expenses.api.security.AuthMechanism;
import com.h4j4x.expenses.api.service.UserAccountService;
import io.quarkus.security.identity.request.TokenAuthenticationRequest;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.quarkus.vertx.http.runtime.security.HttpCredentialTransport;
import io.smallrye.graphql.client.GraphQLClient;
import io.smallrye.graphql.client.core.*;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class UserAccountResourceTests {
    @InjectMock
    AuthMechanism authMechanism;

    @InjectMock
    UserAccountService accountService;

    @Inject
    @GraphQLClient("graphql")
    DynamicGraphQLClient gqlClient;

    @Inject
    DataGenerator dataGen;

    private UserEntity user;

    @BeforeEach
    void setUp() {
        Mockito
            .when(authMechanism.getCredentialTypes())
            .thenReturn(Collections.singleton(TokenAuthenticationRequest.class));
        Mockito
            .when(authMechanism.getCredentialTransport(Mockito.any()))
            .thenReturn(Uni.createFrom().item(new HttpCredentialTransport(
                HttpCredentialTransport.Type.AUTHORIZATION, "Bearer")));
        Mockito
            .when(authMechanism.sendChallenge(Mockito.any()))
            .thenReturn(Uni.createFrom().item(true));
        user = new UserEntity(dataGen.genUserName(), dataGen.genUserEmail(), dataGen.genUserPassword());
        user.setId(dataGen.genRandomLong());
        var identity = QuarkusSecurityIdentity.builder().setPrincipal(user).build();
        Mockito
            .when(authMechanism.authenticate(Mockito.any(), Mockito.any()))
            .thenReturn(Uni.createFrom().item(identity));
    }

    @Test
    public void whenCreateAccount_Then_ShouldGetCreatedAccount() {
        var account = new UserAccount(user, dataGen.genProductName());
        account.setId(dataGen.genRandomLong());
        Mockito
            .when(accountService.addAccount(Mockito.any(), Mockito.any()))
            .thenReturn(Uni.createFrom().item(account));

        var query = Document.document(
            Operation.operation(
                OperationType.MUTATION,
                Field.field("addUserAccount",
                    List.of(
                        Argument.arg("account", InputObject.inputObject(
                            InputObjectField.prop("name", account.getName()),
                            InputObjectField.prop("balance", account.getBalance())
                        ))
                    ),
                    Field.field("key"),
                    Field.field("name"),
                    Field.field("balance")
                )
            )
        );
        var subscriber = gqlClient.executeAsync(query)
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        var response = subscriber
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();
        assertTrue(response.hasData());

        var accountData = response.getObject(UserAccountDTO.class, "addUserAccount");
        assertNotNull(accountData);
        assertEquals(account.getKey(), accountData.getKey());
        assertEquals(account.getName(), accountData.getName());
        assertEquals(account.getBalance(), accountData.getBalance());

        Mockito.verify(accountService).addAccount(Mockito.any(), Mockito.any());
        Mockito.verifyNoMoreInteractions(accountService);
    }

    @Test
    public void whenFindUserAccounts_Then_ShouldGetUserAccounts() {
        var itemsCount = dataGen.genRandomNumber(5, 10);
        Map<Long, UserAccount> items = new HashMap<>();
        for (int i = 0; i < itemsCount; i++) {
            var account = new UserAccount(user, dataGen.genProductName());
            account.setId(dataGen.genRandomLong());
            items.put(account.getId(), account);
        }
        Mockito
            .when(accountService.getAccounts(user))
            .thenReturn(Uni.createFrom().item(items.values().stream().toList()));

        var query = Document.document(
            Operation.operation(
                Field.field("userAccounts",
                    Field.field("key"),
                    Field.field("name"),
                    Field.field("balance")
                )
            )
        );
        var subscriber = gqlClient.executeAsync(query)
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        var response = subscriber
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();
        assertTrue(response.hasData());

        var list = response.getList(UserAccountDTO.class, "userAccounts");
        assertEquals(itemsCount, list.size());
        list.forEach(account -> {
            Long userId = UserAccount.parseUserId(account.getKey());
            assertEquals(user.getId(), userId);
            Long accountId = UserAccount.parseAccountId(account.getKey());
            UserAccount userAccount = items.get(accountId);
            assertNotNull(userAccount);
            assertEquals(userAccount.getKey(), account.getKey());
            assertEquals(userAccount.getName(), account.getName());
            assertEquals(userAccount.getBalance(), account.getBalance());
        });

        Mockito.verify(accountService).getAccounts(Mockito.any());
        Mockito.verifyNoMoreInteractions(accountService);
    }

    @Test
    public void whenEditUserAccount_WithNewName_Then_ShouldGetUpdatedUserAccount() {
        var account = new UserAccount(user, dataGen.genProductName());
        account.setId(dataGen.genRandomLong());
        var edited = new UserAccount(user, dataGen.genProductName());
        edited.setId(account.getId());
        UserAccountDTO accountDTO = new UserAccountDTO(edited.getName());
        Mockito
            .when(accountService.editAccount(user, account.getKey(), accountDTO))
            .thenReturn(Uni.createFrom().item(edited));

        var query = Document.document(
            Operation.operation(
                OperationType.MUTATION,
                Field.field("editUserAccount",
                    List.of(
                        Argument.arg("key", account.getKey()),
                        Argument.arg("account", InputObject.inputObject(
                            InputObjectField.prop("name", edited.getName()),
                            InputObjectField.prop("balance", edited.getBalance())
                        ))
                    ),
                    Field.field("key"),
                    Field.field("name"),
                    Field.field("balance")
                )
            )
        );
        var subscriber = gqlClient.executeAsync(query)
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        var response = subscriber
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();
        assertTrue(response.hasData());

        var userAccount = response.getObject(UserAccountDTO.class, "editUserAccount");
        assertEquals(edited.getKey(), userAccount.getKey());
        assertEquals(edited.getName(), userAccount.getName());
        assertEquals(edited.getBalance(), userAccount.getBalance());

        Mockito.verify(accountService).editAccount(user, account.getKey(), accountDTO);
        Mockito.verifyNoMoreInteractions(accountService);
    }
}
