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
import java.util.List;
import javax.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        var user = new UserEntity(dataGen.genUserName(), dataGen.genUserEmail(), dataGen.genUserPassword());
        user.setId(1L);
        var identity = QuarkusSecurityIdentity.builder().setPrincipal(user).build();
        Mockito
            .when(authMechanism.authenticate(Mockito.any(), Mockito.any()))
            .thenReturn(Uni.createFrom().item(identity));
    }

    @Test
    public void whenCreateAccount_Then_ShouldGetCreatedAccount() {
        var account = new UserAccount(dataGen.genProductName());
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
        assertEquals(account.getName(), accountData.getName());
        assertEquals(account.getBalance(), accountData.getBalance());

        Mockito.verify(accountService).addAccount(Mockito.any(), Mockito.any());
        Mockito.verifyNoMoreInteractions(accountService);
    }
}
