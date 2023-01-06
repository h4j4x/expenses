package com.h4j4x.expenses.api.resource;

import com.h4j4x.expenses.api.DataGenerator;
import com.h4j4x.expenses.api.TestConstants;
import com.h4j4x.expenses.api.domain.UserEntity;
import com.h4j4x.expenses.api.model.UserDTO;
import com.h4j4x.expenses.api.service.UserService;
import io.quarkus.security.identity.request.TokenAuthenticationRequest;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.quarkus.smallrye.jwt.runtime.auth.JWTAuthMechanism;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.quarkus.vertx.http.runtime.security.HttpCredentialTransport;
import io.smallrye.graphql.client.GraphQLClient;
import io.smallrye.graphql.client.InvalidResponseException;
import io.smallrye.graphql.client.core.*;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class UserResourceTests {
    @InjectMock
    JWTAuthMechanism jwtAuth;

    @InjectMock
    UserService userService;

    @Inject
    @GraphQLClient("graphql")
    DynamicGraphQLClient gqlClient;

    @Inject
    DataGenerator dataGen;

    private UserEntity user;

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
        user = new UserEntity(dataGen.genUserName(), dataGen.genUserEmail(), dataGen.genUserPassword());
        user.setId(dataGen.genRandomLong());
        Mockito
            .when(userService.findUserByEmail(user.getName()))
            .thenReturn(Uni.createFrom().item(user));
        Mockito
            .when(userService.findUserByEmail(user.getEmail()))
            .thenReturn(Uni.createFrom().item(user));
    }

    @Test
    public void whenQueryUser_Anonymous_Then_ShouldThrow401() {
        Mockito
            .when(jwtAuth.sendChallenge(Mockito.any()))
            .thenReturn(Uni.createFrom().item(false));
        Mockito
            .when(jwtAuth.authenticate(Mockito.any(), Mockito.any()))
            .thenReturn(Uni.createFrom().optional(Optional.empty()));

        var query = Document.document(
            Operation.operation(
                Field.field("user")
            )
        );
        var subscriber = gqlClient.executeAsync(query)
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

        var query = Document.document(
            Operation.operation(
                Field.field("user",
                    Field.field("name"),
                    Field.field("email")
                )
            )
        );
        var subscriber = gqlClient.executeAsync(query)
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        var response = subscriber
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();
        assertTrue(response.hasData());

        var userData = response.getObject(UserDTO.class, "user");
        assertEquals(user.getName(), userData.getName());
        assertEquals(user.getEmail(), userData.getEmail());

        Mockito.verify(userService).findUserByEmail(user.getName());
        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    public void whenMutateUser_WithoutPassword_Then_ShouldGetUpdatedUserData() {
        var userDTO = new UserDTO(dataGen.genUserName(), dataGen.genUserEmail(), null);
        var updatedEntity = new UserEntity(userDTO.getName(), userDTO.getEmail(), user.getPassword());
        Mockito
            .when(userService.editUser(user, userDTO))
            .thenReturn(Uni.createFrom().item(updatedEntity));

        authenticateUser();

        var query = Document.document(
            Operation.operation(
                OperationType.MUTATION,
                Field.field("editUser",
                    List.of(
                        Argument.arg("user", InputObject.inputObject(
                            InputObjectField.prop("name", userDTO.getName()),
                            InputObjectField.prop("email", userDTO.getEmail())
                        ))
                    ),
                    Field.field("name"),
                    Field.field("email")
                )
            )
        );
        var subscriber = gqlClient.executeAsync(query)
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        var response = subscriber
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();
        assertTrue(response.hasData());

        var userData = response.getObject(UserDTO.class, "editUser");
        assertEquals(userDTO.getName(), userData.getName());
        assertEquals(userDTO.getEmail(), userData.getEmail());

        Mockito.verify(userService).editUser(user, userDTO);
        Mockito.verify(userService).findUserByEmail(user.getName());
        Mockito.verifyNoMoreInteractions(userService);
    }

    private void authenticateUser() {
        var identity = QuarkusSecurityIdentity.builder().setPrincipal(user).build();
        Mockito
            .when(jwtAuth.authenticate(Mockito.any(), Mockito.any()))
            .thenReturn(Uni.createFrom().item(identity));
    }
}
