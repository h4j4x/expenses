package com.h4j4x.expenses.api.service;

import com.h4j4x.expenses.api.DataGenerator;
import com.h4j4x.expenses.api.TestConstants;
import com.h4j4x.expenses.api.domain.UserCategory;
import com.h4j4x.expenses.api.domain.UserEntity;
import com.h4j4x.expenses.api.model.UserCategoryDTO;
import com.h4j4x.expenses.api.repository.UserCategoryRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class UserCategoryServiceTests {
    @InjectMock
    UserCategoryRepository categoryRepo;

    @Inject
    UserCategoryService categoryService;

    @Inject
    DataGenerator dataGen;

    @Test
    void whenCreateCategory_WithRepeatedName_Then_ShouldThrowBadRequest() {
        var user = new UserEntity(dataGen.genUserName(), dataGen.genUserEmail(), dataGen.genUserPassword());
        var category = new UserCategory(user, dataGen.genProductName());
        category.setId(dataGen.genRandomLong());
        Mockito
            .when(categoryRepo.countByUserAndName(user, category.getName()))
            .thenReturn(Uni.createFrom().item(1L));

        var uni = categoryService.addCategory(user, new UserCategoryDTO(category.getName()));
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        subscriber
            .awaitFailure(TestConstants.UNI_DURATION)
            .assertFailedWith(BadRequestException.class, UserCategoryService.CATEGORY_NAME_EXISTS_MESSAGE);

        Mockito.verify(categoryRepo).countByUserAndName(user, category.getName());
        Mockito.verifyNoMoreInteractions(categoryRepo);
    }

    @Test
    void whenCreateCategory_Then_ShouldCreateUserCategory() {
        var user = new UserEntity(dataGen.genUserName(), dataGen.genUserEmail(), dataGen.genUserPassword());
        var category = new UserCategory(user, dataGen.genProductName());
        category.setId(dataGen.genRandomLong());
        Mockito
            .when(categoryRepo.countByUserAndName(user, category.getName()))
            .thenReturn(Uni.createFrom().item(0L));
        Mockito
            .when(categoryRepo.save(Mockito.any()))
            .thenReturn(Uni.createFrom().item(category));

        var uni = categoryService.addCategory(user, new UserCategoryDTO(category.getName()));
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        var userCategory = subscriber
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();
        assertNotNull(userCategory);
        assertEquals(category.getKey(), userCategory.getKey());
        assertEquals(category.getName(), userCategory.getName());

        Mockito.verify(categoryRepo).countByUserAndName(user, category.getName());
        Mockito.verify(categoryRepo).save(Mockito.any());
        Mockito.verifyNoMoreInteractions(categoryRepo);
    }

    @Test
    void whenGetCategories_Then_ShouldGetUserCategories() {
        var user = new UserEntity(dataGen.genUserName(), dataGen.genUserEmail(), dataGen.genUserPassword());
        user.setId(dataGen.genRandomLong());
        var itemsCount = dataGen.genRandomNumber(5, 10);
        List<UserCategory> items = new ArrayList<>(itemsCount);
        for (int i = 0; i < itemsCount; i++) {
            var item = new UserCategory(user, dataGen.genProductName());
            item.setId(dataGen.genRandomLong());
            items.add(item);
        }
        Mockito
            .when(categoryRepo.findAllByUser(user))
            .thenReturn(Uni.createFrom().item(items));

        var uni = categoryService.getCategories(user);
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        var list = subscriber
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();
        assertNotNull(list);
        assertEquals(itemsCount, list.size());
        items.forEach(item -> assertTrue(list.contains(item)));

        Mockito.verify(categoryRepo).findAllByUser(user);
        Mockito.verifyNoMoreInteractions(categoryRepo);
    }

    @Test
    void whenEditCategory_WithInvalidUser_Then_ShouldThrow404() {
        var user = new UserEntity(dataGen.genUserName(), dataGen.genUserEmail(), dataGen.genUserPassword());
        user.setId(dataGen.genRandomLong());
        var otherUser = new UserEntity(dataGen.genUserName(), dataGen.genUserEmail(), dataGen.genUserPassword());
        otherUser.setId(dataGen.genRandomLong());
        var category = new UserCategory(otherUser, dataGen.genProductName());
        category.setId(dataGen.genRandomLong());

        var uni = categoryService.editCategory(user, category.getKey(), new UserCategoryDTO(dataGen.genProductName()));
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        subscriber
            .awaitFailure(TestConstants.UNI_DURATION)
            .assertFailedWith(NotFoundException.class, UserCategoryService.CATEGORY_NOT_FOUND_MESSAGE);
        Mockito.verifyNoMoreInteractions(categoryRepo);
    }

    @Test
    void whenEditCategory_WithInvalidCategory_Then_ShouldThrow404() {
        var user = new UserEntity(dataGen.genUserName(), dataGen.genUserEmail(), dataGen.genUserPassword());
        user.setId(dataGen.genRandomLong());
        var category = new UserCategory(user, dataGen.genProductName());
        category.setId(dataGen.genRandomLong());
        Mockito
            .when(categoryRepo.findByUserAndId(user, category.getId()))
            .thenReturn(Uni.createFrom().optional(Optional.empty()));

        var uni = categoryService.editCategory(user, category.getKey(), new UserCategoryDTO(dataGen.genProductName()));
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        subscriber
            .awaitFailure(TestConstants.UNI_DURATION)
            .assertFailedWith(NotFoundException.class, UserCategoryService.CATEGORY_NOT_FOUND_MESSAGE);
        Mockito.verify(categoryRepo).findByUserAndId(user, category.getId());
        Mockito.verifyNoMoreInteractions(categoryRepo);
    }

    @Test
    void whenEditCategory_WithExistentName_Then_ShouldThrow400() {
        var user = new UserEntity(dataGen.genUserName(), dataGen.genUserEmail(), dataGen.genUserPassword());
        user.setId(dataGen.genRandomLong());
        var category = new UserCategory(user, dataGen.genProductName());
        category.setId(dataGen.genRandomLong());
        UserCategoryDTO categoryDTO = new UserCategoryDTO(dataGen.genProductName());
        Mockito
            .when(categoryRepo.findByUserAndId(user, category.getId()))
            .thenReturn(Uni.createFrom().item(category));
        Mockito
            .when(categoryRepo.countByUserAndNameAndNotId(user, categoryDTO.getName(), category.getId()))
            .thenReturn(Uni.createFrom().item(1L));

        var uni = categoryService.editCategory(user, category.getKey(), categoryDTO);
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        subscriber
            .awaitFailure(TestConstants.UNI_DURATION)
            .assertFailedWith(BadRequestException.class, UserCategoryService.CATEGORY_NAME_EXISTS_MESSAGE);
        Mockito.verify(categoryRepo).findByUserAndId(user, category.getId());
        Mockito.verify(categoryRepo).countByUserAndNameAndNotId(user, categoryDTO.getName(), category.getId());
        Mockito.verifyNoMoreInteractions(categoryRepo);
    }

    @Test
    void whenEditCategory_WithNewName_Then_ShouldEditUserCategory() {
        var user = new UserEntity(dataGen.genUserName(), dataGen.genUserEmail(), dataGen.genUserPassword());
        user.setId(dataGen.genRandomLong());
        var category = new UserCategory(user, dataGen.genProductName());
        category.setId(dataGen.genRandomLong());
        var edited = new UserCategory(user, dataGen.genProductName());
        edited.setId(category.getId());
        Mockito
            .when(categoryRepo.findByUserAndId(user, category.getId()))
            .thenReturn(Uni.createFrom().item(category));
        Mockito
            .when(categoryRepo.countByUserAndNameAndNotId(user, edited.getName(), category.getId()))
            .thenReturn(Uni.createFrom().item(0L));
        Mockito
            .when(categoryRepo.save(edited))
            .thenReturn(Uni.createFrom().item(edited));

        var uni = categoryService.editCategory(user, category.getKey(), UserCategoryDTO.fromCategory(edited));
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        var userCategory = subscriber
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();
        assertNotNull(userCategory);
        assertEquals(edited.getKey(), userCategory.getKey());
        assertEquals(edited.getName(), userCategory.getName());

        Mockito.verify(categoryRepo).findByUserAndId(user, category.getId());
        Mockito.verify(categoryRepo).countByUserAndNameAndNotId(user, edited.getName(), category.getId());
        Mockito.verify(categoryRepo).save(edited);
        Mockito.verifyNoMoreInteractions(categoryRepo);
    }
}
