package com.h4j4x.expenses.api.resource;

import com.h4j4x.expenses.api.DataGenerator;
import com.h4j4x.expenses.api.TestConstants;
import com.h4j4x.expenses.api.client.UserAccountResourceClient;
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
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import java.util.Collections;
import javax.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
public class UserAccountResourceTests {
    @InjectMock
    AuthMechanism authMechanism;

    @InjectMock
    UserAccountService accountService;

    @Inject
    UserAccountResourceClient accountClient;

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

        var uni = accountClient.addUserAccount(new UserAccountDTO(account.getName()));
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        var userAccount = subscriber
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();
        assertNotNull(userAccount);
        assertEquals(account.getName(), userAccount.getName());
        assertEquals(account.getBalance(), userAccount.getBalance());

        Mockito.verify(accountService).addAccount(Mockito.any(), Mockito.any());
        Mockito.verifyNoMoreInteractions(accountService);
    }
}
