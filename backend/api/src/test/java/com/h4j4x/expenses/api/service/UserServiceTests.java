package com.h4j4x.expenses.api.service;

import com.h4j4x.expenses.api.DataGenerator;
import com.h4j4x.expenses.api.TestConstants;
import com.h4j4x.expenses.api.domain.UserEntity;
import com.h4j4x.expenses.api.model.UserCredentials;
import com.h4j4x.expenses.api.model.UserDTO;
import com.h4j4x.expenses.api.repository.UserRepository;
import com.h4j4x.expenses.api.security.impl.DummyStringHasher;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class UserServiceTests {
    @InjectMock
    UserRepository userRepo;
    @Inject
    DataGenerator dataGen;
    private UserService userService;
    private UserEntity user;

    @BeforeEach
    void setUp() {
        user = new UserEntity(dataGen.genUserName(), dataGen.genUserEmail(), dataGen.genUserPassword());
        user.setId(1L);
        Mockito
            .when(userRepo.findByEmail(user.getEmail()))
            .thenReturn(Uni.createFrom().item(user));
        Mockito
            .when(userRepo.countByEmail(user.getEmail()))
            .thenReturn(Uni.createFrom().item(1L));
        userService = new UserService(userRepo, new DummyStringHasher());
    }

    @Test
    void whenCreateUser_WithTestUserEmail_Then_ShouldThrowBadRequest() {
        var uni = userService.createUser(new UserDTO(user.getName(), user.getEmail(), user.getPassword()));
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        subscriber
            .awaitFailure(TestConstants.UNI_DURATION)
            .assertFailedWith(BadRequestException.class, UserService.USER_EMAIL_EXISTS_MESSAGE);

        Mockito.verify(userRepo).countByEmail(user.getEmail());
        Mockito.verifyNoMoreInteractions(userRepo);
    }

    @Test
    void whenCreateUser_WithOtherEmail_Then_ShouldCreateUserEntity() {
        var user = new UserEntity(dataGen.genUserName(), dataGen.genUserEmail(), dataGen.genUserPassword());
        Mockito
            .when(userRepo.countByEmail(user.getEmail()))
            .thenReturn(Uni.createFrom().item(0L));
        Mockito
            .when(userRepo.save(Mockito.any()))
            .thenReturn(Uni.createFrom().item(user));

        var uni = userService.createUser(new UserDTO(user.getName(), user.getEmail(), user.getPassword()));
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        var userEntity = subscriber
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();
        assertNotNull(userEntity);
        assertEquals(user.getEmail(), userEntity.getEmail());

        Mockito.verify(userRepo).countByEmail(user.getEmail());
        Mockito.verify(userRepo).save(Mockito.any());
        Mockito.verifyNoMoreInteractions(userRepo);
    }

    @Test
    void whenFindUser_WithTestUserEmail_Then_ShouldGetTestUser() {
        var uni = userService.findUserByEmail(user.getEmail());
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        var userEntity = subscriber
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();
        assertNotNull(userEntity);
        assertEquals(user.getEmail(), userEntity.getEmail());

        Mockito.verify(userRepo).findByEmail(user.getEmail());
        Mockito.verifyNoMoreInteractions(userRepo);
    }

    @Test
    void whenFindUserByEmailAndPassword_WithCorrectPassword_ThenShouldGetTestUser() {
        var uni = userService.findUserByEmailAndPassword(new UserCredentials(user.getEmail(), user.getPassword()));
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        var userEntity = subscriber
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();
        assertNotNull(userEntity);
        assertEquals(user.getEmail(), userEntity.getEmail());

        Mockito.verify(userRepo).findByEmail(user.getEmail());
        Mockito.verifyNoMoreInteractions(userRepo);
    }

    @Test
    void whenFindUserByEmailAndPassword_WithIncorrectPassword_ThenShouldGetNothing() {
        var uni = userService.findUserByEmailAndPassword(new UserCredentials(user.getEmail(), dataGen.genUserPassword()));
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        var userEntity = subscriber
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();
        assertNull(userEntity);

        Mockito.verify(userRepo).findByEmail(user.getEmail());
        Mockito.verifyNoMoreInteractions(userRepo);
    }

    @Test
    void whenEditUser_WithoutPassword_Then_ShouldEditUserEntityAndKeepPassword() {
        var user = new UserEntity(dataGen.genUserName(), "other-" + dataGen.genUserEmail(), dataGen.genUserPassword());
        user.setId(10L);
        var edited = new UserEntity(user.getName(), "another-" + dataGen.genUserEmail(), dataGen.genUserPassword());
        edited.setId(user.getId());
        Mockito
            .when(userRepo.countByEmailAndNotId(edited.getEmail(), edited.getId()))
            .thenReturn(Uni.createFrom().item(0L));
        Mockito
            .when(userRepo.save(edited))
            .thenReturn(Uni.createFrom().item(edited));

        var uni = userService.editUser(user, new UserDTO(user.getName(), edited.getEmail(), null));
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        var userEntity = subscriber
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();
        assertNotNull(userEntity);
        assertEquals(edited.getName(), userEntity.getName());
        assertEquals(edited.getEmail(), userEntity.getEmail());
        assertEquals(edited.getPassword(), userEntity.getPassword());

        Mockito.verify(userRepo).countByEmailAndNotId(edited.getEmail(), edited.getId());
        Mockito.verify(userRepo).save(edited);
        Mockito.verifyNoMoreInteractions(userRepo);
    }

    @Test
    void whenEditUser_WithExistentEmail_Then_ShouldThrow400() {
        var user = new UserEntity(dataGen.genUserName(), "other-" + dataGen.genUserEmail(), dataGen.genUserPassword());
        user.setId(10L);
        var newEmail = "another-" + dataGen.genUserEmail();
        Mockito
            .when(userRepo.countByEmailAndNotId(newEmail, user.getId()))
            .thenReturn(Uni.createFrom().item(1L));

        var uni = userService.editUser(user, new UserDTO(user.getName(), newEmail, null));
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        subscriber
            .awaitFailure(TestConstants.UNI_DURATION)
            .assertFailedWith(BadRequestException.class, UserService.USER_EMAIL_EXISTS_MESSAGE);

        Mockito.verify(userRepo).countByEmailAndNotId(newEmail, user.getId());
        Mockito.verifyNoMoreInteractions(userRepo);
    }
}
