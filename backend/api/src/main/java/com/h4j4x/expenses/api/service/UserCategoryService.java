package com.h4j4x.expenses.api.service;

import com.h4j4x.expenses.api.domain.UserCategory;
import com.h4j4x.expenses.api.domain.UserEntity;
import com.h4j4x.expenses.api.model.UserCategoryDTO;
import com.h4j4x.expenses.api.repository.UserCategoryRepository;
import io.smallrye.mutiny.Uni;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;

@ApplicationScoped
public class UserCategoryService {
    public static final String CATEGORY_NAME_EXISTS_MESSAGE = "Category name already registered";
    public static final String CATEGORY_NOT_FOUND_MESSAGE = "Category not found";

    private final UserCategoryRepository categoryRepo;

    public UserCategoryService(UserCategoryRepository categoryRepo) {
        this.categoryRepo = categoryRepo;
    }

    public Uni<UserCategory> addCategory(UserEntity user, UserCategoryDTO category) {
        return categoryRepo.countByUserAndName(user, category.getName())
            .onItem().transform(count -> {
                if (count > 0) {
                    return null;
                }
                return count;
            })
            .onItem().ifNull().failWith(new BadRequestException(CATEGORY_NAME_EXISTS_MESSAGE))
            .onItem().ifNotNull().transformToUni(c -> createCategory(user, category));
    }

    private Uni<UserCategory> createCategory(UserEntity user, UserCategoryDTO category) {
        var userCategory = new UserCategory(user, category.getName());
        return categoryRepo.save(userCategory);
    }

    public Uni<List<UserCategory>> getCategories(UserEntity user) {
        return categoryRepo.findAllByUser(user);
    }

    public Uni<UserCategory> editCategory(UserEntity user, String key, UserCategoryDTO category) {
        Long userId = UserCategory.parseUserId(key);
        if (!user.getId().equals(userId)) {
            return Uni.createFrom().failure(new NotFoundException(CATEGORY_NOT_FOUND_MESSAGE));
        }
        Long id = UserCategory.parseCategoryId(key);
        return categoryRepo.findByUserAndId(user, id)
            .onItem().ifNull().failWith(new NotFoundException(CATEGORY_NOT_FOUND_MESSAGE))
            .onItem().ifNotNull().call(userCategory -> categoryRepo
                .countByUserAndNameAndNotId(user, category.getName(), userCategory.getId())
                .onItem().transform(count -> {
                    if (count > 0) {
                        return null;
                    }
                    return count;
                })
                .onItem().ifNull().failWith(new BadRequestException(CATEGORY_NAME_EXISTS_MESSAGE)))
            .onItem().ifNotNull().transformToUni(userCategory -> {
                userCategory.setName(category.getName());
                return categoryRepo.save(userCategory);
            });
    }
}
