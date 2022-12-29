package com.h4j4x.expenses.api.service;

import com.h4j4x.expenses.api.domain.UserEntity;
import com.h4j4x.expenses.api.repository.UserRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.smallrye.mutiny.Uni;
import java.time.Duration;
import javax.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class UserServiceTests {
    private static final String TEST_EMAIL = "test@mail.com";
    private static final String TEST_PASSWORD = "12345678";

    @Inject
    UserService userService;

    @InjectMock
    UserRepository userRepo;

    @BeforeEach
    void setUp() {
        var user = new UserEntity();
        user.setId(1L);
        user.setName("TEST");
        user.setEmail(TEST_EMAIL);
        user.setPassword(TEST_PASSWORD);
        Mockito
            .when(userRepo.findByEmail(TEST_EMAIL))
            .thenReturn(Uni.createFrom().item(user));
    }

    @Test
    void whenFindUserByTestUserEmail_Then_ShouldGetTestUser() {
        UserEntity userEntity = userService.findUserByEmail(TEST_EMAIL)
            .await().atMost(Duration.ofMillis(10));
        assertNotNull(userEntity);
        assertEquals(TEST_EMAIL, userEntity.getEmail());

        Mockito.verify(userRepo).findByEmail(TEST_EMAIL);
    }

    @Test
    void whenFindUserByEmailAndPassword_WithCorrectPassword_ThenShouldGetTestUser() {
        UserEntity userEntity = userService.findUserByEmailAndPassword(TEST_EMAIL, TEST_PASSWORD)
            .await().atMost(Duration.ofMillis(10));
        assertNotNull(userEntity);
        assertEquals(TEST_EMAIL, userEntity.getEmail());

        Mockito.verify(userRepo).findByEmail(TEST_EMAIL);
    }

    @Test
    void whenFindUserByEmailAndPassword_WithIncorrectPassword_ThenShouldGetNothing() {
        UserEntity userEntity = userService.findUserByEmailAndPassword(TEST_EMAIL, TEST_PASSWORD + "-")
            .await().atMost(Duration.ofMillis(10));
        assertNull(userEntity);

        Mockito.verify(userRepo).findByEmail(TEST_EMAIL);
    }
}
