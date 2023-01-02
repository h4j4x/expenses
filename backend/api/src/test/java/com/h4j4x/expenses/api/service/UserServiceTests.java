package com.h4j4x.expenses.api.service;

import com.h4j4x.expenses.api.domain.UserEntity;
import com.h4j4x.expenses.api.model.UserCredentials;
import com.h4j4x.expenses.api.model.UserDTO;
import com.h4j4x.expenses.api.repository.UserRepository;
import com.h4j4x.expenses.api.security.impl.DummyStringHasher;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import java.time.Duration;
import java.util.Optional;
import javax.ws.rs.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@QuarkusTest
public class UserServiceTests {
    private static final String TEST_EMAIL = "test@mail.com";
    private static final String TEST_PASSWORD = "12345678";

    private UserService userService;

    @InjectMock
    UserRepository userRepo;

    @BeforeEach
    void setUp() {
        var user = new UserEntity("TEST", TEST_EMAIL, TEST_PASSWORD);
        user.setId(1L);
        Mockito
            .when(userRepo.findByEmail(TEST_EMAIL))
            .thenReturn(Uni.createFrom().item(user));
        userService = new UserService(userRepo, new DummyStringHasher());
    }

    @Test
    void whenCreateUser_WithTestUserEmail_Then_ShouldThrowBadRequest() {
        var uni = userService.createUser(new UserDTO("TEST", TEST_EMAIL, TEST_PASSWORD));
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        subscriber
            .awaitFailure(Duration.ofMillis(100))
            .assertFailedWith(BadRequestException.class, UserService.USER_EMAIL_EXISTS_MESSAGE);

        Mockito.verify(userRepo).findByEmail(TEST_EMAIL);
        Mockito.verifyNoMoreInteractions(userRepo);
    }

    @Test
    void whenCreateUser_WithOtherEmail_Then_ShouldCreateUserEntity() {
        var user = new UserEntity("TEST", "other-" + TEST_EMAIL, TEST_PASSWORD);
        Mockito
            .when(userRepo.findByEmail(user.getEmail()))
            .thenReturn(Uni.createFrom().optional(Optional.empty()));
        Mockito
            .when(userRepo.save(any(UserEntity.class)))
            .thenReturn(Uni.createFrom().item(user));

        var uni = userService.createUser(new UserDTO("TEST", "other-" + TEST_EMAIL, TEST_PASSWORD));
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        var userEntity = subscriber
            .awaitItem(Duration.ofMillis(100))
            .getItem();
        assertNotNull(userEntity);
        assertEquals(user.getEmail(), userEntity.getEmail());

        Mockito.verify(userRepo).findByEmail(user.getEmail());
        Mockito.verify(userRepo).save(any(UserEntity.class));
        Mockito.verifyNoMoreInteractions(userRepo);
    }

    @Test
    void whenFindUser_WithTestUserEmail_Then_ShouldGetTestUser() {
        var uni = userService.findUserByEmail(TEST_EMAIL);
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        var userEntity = subscriber
            .awaitItem(Duration.ofMillis(100))
            .getItem();
        assertNotNull(userEntity);
        assertEquals(TEST_EMAIL, userEntity.getEmail());

        Mockito.verify(userRepo).findByEmail(TEST_EMAIL);
        Mockito.verifyNoMoreInteractions(userRepo);
    }

    @Test
    void whenFindUserByEmailAndPassword_WithCorrectPassword_ThenShouldGetTestUser() {
        var uni = userService.findUserByEmailAndPassword(new UserCredentials(TEST_EMAIL, TEST_PASSWORD));
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        var userEntity = subscriber
            .awaitItem(Duration.ofMillis(100))
            .getItem();
        assertNotNull(userEntity);
        assertEquals(TEST_EMAIL, userEntity.getEmail());

        Mockito.verify(userRepo).findByEmail(TEST_EMAIL);
        Mockito.verifyNoMoreInteractions(userRepo);
    }

    @Test
    void whenFindUserByEmailAndPassword_WithIncorrectPassword_ThenShouldGetNothing() {
        var uni = userService.findUserByEmailAndPassword(new UserCredentials(TEST_EMAIL, TEST_PASSWORD + "-"));
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        var userEntity = subscriber
            .awaitItem(Duration.ofMillis(100))
            .getItem();
        assertNull(userEntity);

        Mockito.verify(userRepo).findByEmail(TEST_EMAIL);
        Mockito.verifyNoMoreInteractions(userRepo);
    }

    @Test
    void whenEditUser_WithoutPassword_Then_ShouldEditUserEntityAndKeepPassword() {
        var user = new UserEntity("TEST", "other-" + TEST_EMAIL, TEST_PASSWORD);
        var edited = new UserEntity(user.getName(), "another-" + TEST_EMAIL, TEST_PASSWORD);
        Mockito
            .when(userRepo.save(any(UserEntity.class)))
            .thenReturn(Uni.createFrom().item(edited));

        var uni = userService.editUser(user, new UserDTO(user.getName(), edited.getEmail(), null));
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        var userEntity = subscriber
            .awaitItem(Duration.ofMillis(100))
            .getItem();
        assertNotNull(userEntity);
        assertEquals(edited.getName(), userEntity.getName());
        assertEquals(edited.getEmail(), userEntity.getEmail());
        assertEquals(edited.getPassword(), userEntity.getPassword());

        Mockito.verify(userRepo).save(any(UserEntity.class));
        Mockito.verifyNoMoreInteractions(userRepo);
    }
}
