package com.h4j4x.expenses.api.repository;

import com.h4j4x.expenses.api.DataGenerator;
import com.h4j4x.expenses.api.TestConstants;
import com.h4j4x.expenses.api.domain.UserCategory;
import com.h4j4x.expenses.api.domain.UserEntity;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.helpers.test.UniAssertSubscriber;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class UserCategoryRepositoryTests {
    @Inject
    UserCategoryRepository categoryRepo;

    @Inject
    UserRepository userRepo;

    @Inject
    DataGenerator dataGen;

    @Test
    void whenCreateCategory_Invalid_Then_ShouldThrowError() {
        var category = new UserCategory();
        var uni = categoryRepo.save(category);
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        subscriber
            .awaitFailure(TestConstants.UNI_DURATION)
            .assertFailedWith(ConstraintViolationException.class);
    }

    @Test
    void whenCreateCategory_WithoutUser_Then_ShouldThrowError() {
        var category = new UserCategory(dataGen.genProductName());
        var uni = categoryRepo.save(category);
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        subscriber
            .awaitFailure(TestConstants.UNI_DURATION)
            .assertFailedWith(ConstraintViolationException.class);
    }

    @Test
    void whenCreateCategory_Then_ShouldAssignId() {
        var user = createUser();
        var category = new UserCategory(user, dataGen.genProductName());
        var uni = categoryRepo.save(category);
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        var userCategory = subscriber
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();
        assertNotNull(userCategory);
        assertNotNull(userCategory.getId());
        assertEquals(category.getName(), userCategory.getName());
    }

    @Test
    void whenFindCategory_ByUserAndName_Then_ShouldGetUserCategory() {
        var user = createUser();
        var category = new UserCategory(user, dataGen.genProductName());
        var uni = categoryRepo.save(category);
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        subscriber
            .awaitItem(TestConstants.UNI_DURATION);

        uni = categoryRepo.findByUserAndName(user, category.getName());
        subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        UserCategory userCategory = subscriber
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();
        assertNotNull(userCategory);
        assertNotNull(userCategory.getId());
        assertEquals(category.getName(), userCategory.getName());
    }

    @Test
    void whenFindCategory_ByUserAndId_Then_ShouldGetUserCategory() {
        var user = createUser();
        var category = new UserCategory(user, dataGen.genProductName());
        var uni = categoryRepo.save(category);
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        category = subscriber
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();

        uni = categoryRepo.findByUserAndId(user, category.getId());
        subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        UserCategory userCategory = subscriber
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();
        assertNotNull(userCategory);
        assertNotNull(userCategory.getId());
        assertEquals(category.getName(), userCategory.getName());
    }

    @Test
    void whenFindCategory_ByUserAndInvalidName_Then_ShouldGetNothing() {
        var user = createUser();
        var category = new UserCategory(user, dataGen.genProductName());
        var uni = categoryRepo.save(category);
        var subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        subscriber
            .awaitItem(TestConstants.UNI_DURATION);

        uni = categoryRepo.findByUserAndName(user, category.getName() + "-");
        subscriber = uni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        UserCategory userCategory = subscriber
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();
        assertNull(userCategory);
    }

    @Test
    void whenCountCategories_ByUser_Then_ShouldGetCount() {
        var user = createUser();
        var itemsCount = dataGen.genRandomNumber(1, 5);
        for (int i = 0; i < itemsCount; i++) {
            var category = new UserCategory(user, dataGen.genProductName());
            categoryRepo.save(category)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .awaitItem(TestConstants.UNI_DURATION);
        }

        var countUni = categoryRepo.countByUser(user);
        var countSubscriber = countUni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        var count = countSubscriber
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();
        assertEquals(itemsCount, count);
    }

    @Test
    void whenFindCategories_ByUser_Then_ShouldGetData() {
        var user = createUser();
        var itemsCount = dataGen.genRandomNumber(5, 10);
        List<UserCategory> items = new ArrayList<>(itemsCount);
        for (int i = 0; i < itemsCount; i++) {
            var category = new UserCategory(user, dataGen.genProductName());
            var item = categoryRepo.save(category)
                .subscribe().withSubscriber(UniAssertSubscriber.create())
                .awaitItem(TestConstants.UNI_DURATION)
                .getItem();
            items.add(item);
        }

        var findUni = categoryRepo.findAllByUser(user);
        var findSubscriber = findUni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        var list = findSubscriber
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();
        assertEquals(itemsCount, list.size());
        items.forEach(item -> assertTrue(list.contains(item)));
    }

    @Test
    void whenCountCategories_ByUserAndNameAndSameId_Then_ShouldGetNothing() {
        var user = createUser();
        var category = new UserCategory(user, dataGen.genProductName());
        var userCategory = categoryRepo.save(category)
            .subscribe().withSubscriber(UniAssertSubscriber.create())
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();

        var countUni = categoryRepo.countByUserAndNameAndNotId(user, userCategory.getName(), userCategory.getId());
        var countSubscriber = countUni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        var count = countSubscriber
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();
        assertEquals(0L, count);
    }

    @Test
    void whenCountCategories_ByUserAndNameAndOtherId_Then_ShouldGetOne() {
        var user = createUser();
        var category = new UserCategory(user, dataGen.genProductName());
        var userCategory = categoryRepo.save(category)
            .subscribe().withSubscriber(UniAssertSubscriber.create())
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();

        var countUni = categoryRepo.countByUserAndNameAndNotId(user, userCategory.getName(), userCategory.getId() + 1);
        var countSubscriber = countUni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        var count = countSubscriber
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();
        assertEquals(1L, count);
    }

    @Test
    void whenCountCategories_ByUserAndName_Then_ShouldGetOne() {
        var user = createUser();
        var category = new UserCategory(user, dataGen.genProductName());
        var userCategory = categoryRepo.save(category)
            .subscribe().withSubscriber(UniAssertSubscriber.create())
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();

        var countUni = categoryRepo.countByUserAndName(user, userCategory.getName());
        var countSubscriber = countUni
            .subscribe().withSubscriber(UniAssertSubscriber.create());

        var count = countSubscriber
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();
        assertEquals(1L, count);
    }

    private UserEntity createUser() {
        var entity = new UserEntity(dataGen.genUserName(), dataGen.genUserEmail(), dataGen.genUserPassword());
        return userRepo.save(entity)
            .subscribe().withSubscriber(UniAssertSubscriber.create())
            .awaitItem(TestConstants.UNI_DURATION)
            .getItem();
    }
}
