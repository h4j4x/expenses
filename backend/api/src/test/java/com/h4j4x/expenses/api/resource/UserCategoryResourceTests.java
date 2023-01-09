package com.h4j4x.expenses.api.resource;

import com.h4j4x.expenses.api.DataGenerator;
import com.h4j4x.expenses.api.TestConstants;
import com.h4j4x.expenses.api.domain.UserCategory;
import com.h4j4x.expenses.api.domain.UserEntity;
import com.h4j4x.expenses.api.model.UserCategoryDTO;
import com.h4j4x.expenses.api.security.AuthMechanism;
import com.h4j4x.expenses.api.service.UserCategoryService;
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
public class UserCategoryResourceTests {
    @InjectMock
    AuthMechanism authMechanism;

    @InjectMock
    UserCategoryService categoryService;

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
    public void whenCreateCategory_Then_ShouldGetCreatedCategory() {
        var category = new UserCategory(user, dataGen.genProductName());
        category.setId(dataGen.genRandomLong());
        Mockito
            .when(categoryService.addCategory(Mockito.any(), Mockito.any()))
            .thenReturn(Uni.createFrom().item(category));

        var query = Document.document(
            Operation.operation(
                OperationType.MUTATION,
                Field.field("addUserCategory",
                    List.of(
                        Argument.arg("category", InputObject.inputObject(
                            InputObjectField.prop("name", category.getName())
                        ))
                    ),
                    Field.field("key"),
                    Field.field("name")
                )
            )
        );
        var subscriber = gqlClient.executeAsync(query)
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        var response = subscriber
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();
        assertTrue(response.hasData());

        var categoryData = response.getObject(UserCategoryDTO.class, "addUserCategory");
        assertNotNull(categoryData);
        assertEquals(category.getKey(), categoryData.getKey());
        assertEquals(category.getName(), categoryData.getName());

        Mockito.verify(categoryService).addCategory(Mockito.any(), Mockito.any());
        Mockito.verifyNoMoreInteractions(categoryService);
    }

    @Test
    public void whenFindUserCategories_Then_ShouldGetUserCategories() {
        var itemsCount = dataGen.genRandomNumber(5, 10);
        Map<Long, UserCategory> items = new HashMap<>();
        for (int i = 0; i < itemsCount; i++) {
            var category = new UserCategory(user, dataGen.genProductName());
            category.setId(dataGen.genRandomLong());
            items.put(category.getId(), category);
        }
        Mockito
            .when(categoryService.getCategories(user))
            .thenReturn(Uni.createFrom().item(items.values().stream().toList()));

        var query = Document.document(
            Operation.operation(
                Field.field("userCategories",
                    Field.field("key"),
                    Field.field("name")
                )
            )
        );
        var subscriber = gqlClient.executeAsync(query)
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        var response = subscriber
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();
        assertTrue(response.hasData());

        var list = response.getList(UserCategoryDTO.class, "userCategories");
        assertEquals(itemsCount, list.size());
        list.forEach(category -> {
            Long userId = UserCategory.parseUserId(category.getKey());
            assertEquals(user.getId(), userId);
            Long categoryId = UserCategory.parseCategoryId(category.getKey());
            UserCategory userCategory = items.get(categoryId);
            assertNotNull(userCategory);
            assertEquals(userCategory.getKey(), category.getKey());
            assertEquals(userCategory.getName(), category.getName());
        });

        Mockito.verify(categoryService).getCategories(Mockito.any());
        Mockito.verifyNoMoreInteractions(categoryService);
    }

    @Test
    public void whenEditUserCategory_WithNewName_Then_ShouldGetUpdatedUserCategory() {
        var category = new UserCategory(user, dataGen.genProductName());
        category.setId(dataGen.genRandomLong());
        var edited = new UserCategory(user, dataGen.genProductName());
        edited.setId(category.getId());
        UserCategoryDTO categoryDTO = new UserCategoryDTO(edited.getName());
        Mockito
            .when(categoryService.editCategory(user, category.getKey(), categoryDTO))
            .thenReturn(Uni.createFrom().item(edited));

        var query = Document.document(
            Operation.operation(
                OperationType.MUTATION,
                Field.field("editUserCategory",
                    List.of(
                        Argument.arg("key", category.getKey()),
                        Argument.arg("category", InputObject.inputObject(
                            InputObjectField.prop("name", edited.getName())
                        ))
                    ),
                    Field.field("key"),
                    Field.field("name")
                )
            )
        );
        var subscriber = gqlClient.executeAsync(query)
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        var response = subscriber
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();
        assertTrue(response.hasData());

        var userCategory = response.getObject(UserCategoryDTO.class, "editUserCategory");
        assertEquals(edited.getKey(), userCategory.getKey());
        assertEquals(edited.getName(), userCategory.getName());

        Mockito.verify(categoryService).editCategory(user, category.getKey(), categoryDTO);
        Mockito.verifyNoMoreInteractions(categoryService);
    }
}
