package com.h4j4x.expenses.api.resource;

import com.h4j4x.expenses.api.TestConstants;
import com.h4j4x.expenses.api.client.UserResourceClient;
import com.h4j4x.expenses.api.domain.UserEntity;
import com.h4j4x.expenses.api.model.UserDTO;
import com.h4j4x.expenses.api.service.UserService;
import io.quarkus.security.identity.request.TokenAuthenticationRequest;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.quarkus.smallrye.jwt.runtime.auth.JWTAuthMechanism;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.quarkus.vertx.http.runtime.security.HttpCredentialTransport;
import io.smallrye.graphql.client.InvalidResponseException;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import java.util.Collections;
import java.util.Optional;
import javax.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class UserResourcesTests {
    private static final String TEST_NAME = "test";
    private static final String TEST_EMAIL = "test@mail.com";
    private static final String TEST_PASSWORD = "12345678";

    @InjectMock
    JWTAuthMechanism jwtAuth;

    @InjectMock
    UserService userService;

    @Inject
    UserResourceClient userClient;

    private UserEntity userEntity() {
        var user = new UserEntity(TEST_NAME, TEST_EMAIL, TEST_PASSWORD);
        user.setId(1L);
        return user;
    }

    @BeforeEach
    void setUp() {
        Mockito
            .when(jwtAuth.getCredentialTypes())
            .thenReturn(Collections.singleton(TokenAuthenticationRequest.class));
        Mockito
            .when(jwtAuth.getCredentialTransport(Mockito.any()))
            .thenReturn(Uni.createFrom().item(new HttpCredentialTransport(
                HttpCredentialTransport.Type.AUTHORIZATION, "Bearer")));
        Mockito
            .when(jwtAuth.sendChallenge(Mockito.any()))
            .thenReturn(Uni.createFrom().item(true));
        Mockito
            .when(userService.findUserByEmail(TEST_EMAIL))
            .thenReturn(Uni.createFrom().item(userEntity()));
    }

    @Test
    public void whenQueryUser_Anonymous_Then_ShouldThrow401() {
        Mockito
            .when(jwtAuth.sendChallenge(Mockito.any()))
            .thenReturn(Uni.createFrom().item(false));
        Mockito
            .when(jwtAuth.authenticate(Mockito.any(), Mockito.any()))
            .thenReturn(Uni.createFrom().optional(Optional.empty()));

        var uni = userClient.getUser();
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        Throwable failure = subscriber
            .awaitFailure(TestConstants.UNI_DURATION)
            .assertFailedWith(InvalidResponseException.class)
            .getFailure();
        assertTrue(failure.getMessage().contains("401"));

        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    public void whenQueryUser_Authenticated_Then_ShouldGetUserData() {
        authenticateUser();

        var uni = userClient.getUser();
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        var user = subscriber
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();
        assertNotNull(user);
        assertEquals(TEST_NAME, user.getName());
        assertEquals(TEST_EMAIL, user.getEmail());

        Mockito.verify(userService).findUserByEmail(TEST_EMAIL);
        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    public void whenMutateUser_WithoutPassword_Then_ShouldGetUpdatedUserData() {
        var userDTO = new UserDTO("New Name", "new-email@mail.com", null);
        var updatedEntity = new UserEntity(userDTO.getName(), userDTO.getEmail(), TEST_PASSWORD);
        Mockito
            .when(userService.editUser(Mockito.any(), Mockito.any()))
            .thenReturn(Uni.createFrom().item(updatedEntity));

        authenticateUser();

        var uni = userClient.editUser(userDTO);
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        var user = subscriber
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();
        assertNotNull(user);
        assertEquals(userDTO.getName(), user.getName());
        assertEquals(userDTO.getEmail(), user.getEmail());

        Mockito.verify(userService).editUser(userEntity(), userDTO);
        Mockito.verify(userService).findUserByEmail(TEST_EMAIL);
        Mockito.verifyNoMoreInteractions(userService);
    }

    private void authenticateUser() {
        UserEntity userEntity = userEntity();
        userEntity.setName(TEST_EMAIL);
        QuarkusSecurityIdentity identity = QuarkusSecurityIdentity.builder().setPrincipal(userEntity).build();
        Mockito
            .when(jwtAuth.authenticate(Mockito.any(), Mockito.any()))
            .thenReturn(Uni.createFrom().item(identity));
    }
}
