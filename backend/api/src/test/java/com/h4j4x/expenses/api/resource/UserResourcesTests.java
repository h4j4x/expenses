package com.h4j4x.expenses.api.resource;

import com.h4j4x.expenses.api.domain.UserEntity;
import com.h4j4x.expenses.api.service.UserService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.smallrye.graphql.client.GraphQLClient;
import io.smallrye.graphql.client.InvalidResponseException;
import io.smallrye.graphql.client.core.Document;
import io.smallrye.graphql.client.dynamic.api.DynamicGraphQLClient;
import io.smallrye.mutiny.Uni;
import java.util.concurrent.ExecutionException;
import javax.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static io.smallrye.graphql.client.core.Document.document;
import static io.smallrye.graphql.client.core.Field.field;
import static io.smallrye.graphql.client.core.Operation.operation;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@QuarkusTest
public class UserResourcesTests {
    private static final String TEST_NAME = "test";
    private static final String TEST_EMAIL = "test@mail.com";
    private static final String TEST_PASSWORD = "12345678";

    @InjectMock
    UserService userService;

    @Inject
    @GraphQLClient("graphql")
    DynamicGraphQLClient graphQLClient;

    @BeforeEach
    void setUp() {
        var user = new UserEntity(TEST_NAME, TEST_EMAIL, TEST_PASSWORD);
        user.setId(1L);
        Mockito
            .when(userService.findUserByEmail(TEST_EMAIL))
            .thenReturn(Uni.createFrom().item(user));
    }

    @Test
    public void whenGetMe_Anonymous_Then_ShouldThrow401() throws ExecutionException, InterruptedException {
        Document query = document(
            operation(
                field("user",
                    field("name"),
                    field("email")
                )
            )
        );
        try {
            graphQLClient.executeSync(query);
            fail("Should throw 401");
            /*assertTrue(response.hasData());
            UserDTO user = response.getObject(UserDTO.class, "user");
            assertNotNull(user);
            assertEquals(TEST_NAME, user.getName());
            assertEquals(TEST_EMAIL, user.getEmail());*/
        } catch (InvalidResponseException e) {
            assertTrue(e.getMessage().contains("401"));
        }

        Mockito.verifyNoMoreInteractions(userService);
    }
}
