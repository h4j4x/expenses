package com.h4j4x.expenses.api.resource;

import com.h4j4x.expenses.api.DataGenerator;
import com.h4j4x.expenses.api.domain.UserEntity;
import com.h4j4x.expenses.api.model.UserCredentials;
import com.h4j4x.expenses.api.model.UserDTO;
import com.h4j4x.expenses.api.model.UserToken;
import com.h4j4x.expenses.api.service.UserService;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.smallrye.mutiny.Uni;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestHTTPEndpoint(AuthResource.class)
public class AuthResourceTests {
    @InjectMock
    UserService userService;

    @Inject
    DataGenerator dataGen;

    private UserEntity user;

    @BeforeEach
    void setUp() {
        user = new UserEntity(dataGen.genUserName(), dataGen.genUserEmail(), dataGen.genUserPassword());
        user.setId(dataGen.genRandomLong());
        Mockito
            .when(userService.findUserByEmail(user.getEmail()))
            .thenReturn(Uni.createFrom().item(user));
        var credentials = new UserCredentials(user.getEmail(), user.getPassword());
        Mockito
            .when(userService.findUserByEmailAndPassword(credentials))
            .thenReturn(Uni.createFrom().item(user));
    }

    @Test
    public void whenPostSignUp_WithTestUserEmail_Then_ShouldThrow400() {
        var user = new UserDTO(dataGen.genUserName(), dataGen.genUserEmail(), dataGen.genUserPassword());
        Mockito
            .when(userService.createUser(user))
            .thenThrow(new BadRequestException(UserService.USER_EMAIL_EXISTS_MESSAGE));
        RestAssured.given()
            .contentType(ContentType.JSON)
            .when().body(user).post(AuthResource.SIGN_UP)
            .then()
            .statusCode(HttpStatus.SC_BAD_REQUEST);

        Mockito.verify(userService).createUser(user);
        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    public void whenPostSignUp_WithOtherEmail_Then_ShouldGetJwtToken() {
        var user = new UserDTO(dataGen.genUserName(), dataGen.genUserEmail(), dataGen.genUserPassword());
        var userEntity = new UserEntity(user.getName(), user.getEmail(), user.getPassword());
        userEntity.setId(dataGen.genRandomLong());
        Mockito
            .when(userService.createUser(user))
            .thenReturn(Uni.createFrom().item(userEntity));
        RestAssured.given()
            .contentType(ContentType.JSON)
            .when().body(user).post(AuthResource.SIGN_UP)
            .then()
            .statusCode(HttpStatus.SC_CREATED)
            .body(not(blankOrNullString()));

        Mockito.verify(userService).createUser(user);
        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    public void whenPostSignIn_WithCorrectCredentials_Then_ShouldGetJwtToken() {
        var credentials = new UserCredentials(user.getEmail(), user.getPassword());
        RestAssured.given()
            .contentType(ContentType.JSON)
            .when().body(credentials).post(AuthResource.SIGN_IN)
            .then()
            .statusCode(HttpStatus.SC_OK)
            .body(not(blankOrNullString()));

        Mockito.verify(userService).findUserByEmailAndPassword(credentials);
        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    public void whenPostSignIn_WithInvalidCredentials_Then_ShouldThrow401() {
        var credentials = new UserCredentials(dataGen.genUserEmail(), dataGen.genUserPassword());
        RestAssured.given()
            .contentType(ContentType.JSON)
            .when().body(credentials).post(AuthResource.SIGN_IN)
            .then()
            .statusCode(HttpStatus.SC_UNAUTHORIZED);

        Mockito.verify(userService).findUserByEmailAndPassword(credentials);
        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    public void whenGetMe_Anonymous_Then_ShouldThrow401() {
        RestAssured.given()
            .when().get(AuthResource.ME)
            .then()
            .statusCode(HttpStatus.SC_UNAUTHORIZED);

        Mockito.verifyNoMoreInteractions(userService);
    }

    @Test
    public void whenGetMe_Authenticated_Then_ShouldGetUserData() {
        var credentials = new UserCredentials(user.getEmail(), user.getPassword());
        var token = RestAssured.given()
            .contentType(ContentType.JSON)
            .when().body(credentials).post(AuthResource.SIGN_IN)
            .then().extract().as(UserToken.class);

        RestAssured.given()
            .headers("Authorization", "Bearer " + token.token())
            .when().get(AuthResource.ME)
            .then()
            .statusCode(HttpStatus.SC_OK)
            .body("email", is(user.getEmail()));

        Mockito.verify(userService).findUserByEmailAndPassword(credentials);
        Mockito.verify(userService).findUserByEmail(user.getEmail());
        Mockito.verifyNoMoreInteractions(userService);
    }
}
