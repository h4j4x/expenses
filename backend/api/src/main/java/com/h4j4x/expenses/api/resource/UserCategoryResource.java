package com.h4j4x.expenses.api.resource;

import com.h4j4x.expenses.api.domain.UserEntity;
import com.h4j4x.expenses.api.model.UserCategoryDTO;
import com.h4j4x.expenses.api.service.UserCategoryService;
import io.quarkus.hibernate.reactive.panache.common.runtime.ReactiveTransactional;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import java.util.List;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
@ReactiveTransactional
public class UserCategoryResource {
    private final SecurityIdentity identity;

    private final UserCategoryService categoryService;

    public UserCategoryResource(SecurityIdentity identity, UserCategoryService categoryService) {
        this.identity = identity;
        this.categoryService = categoryService;
    }

    @Mutation
    @Description("Add user category")
    public Uni<UserCategoryDTO> addUserCategory(UserCategoryDTO category) {
        return Uni.createFrom()
            .item(authEntity())
            .flatMap(userEntity -> categoryService.addCategory(userEntity, category))
            .onItem().transform(UserCategoryDTO::fromCategory);
    }

    @Query
    @Description("Get user categories")
    public Uni<List<UserCategoryDTO>> getUserCategories() {
        return categoryService.getCategories(authEntity())
            .map(userCategories -> userCategories.stream()
                .map(UserCategoryDTO::fromCategory).toList());
    }

    @Mutation
    @Description("Edit user category")
    public Uni<UserCategoryDTO> editUserCategory(String key, UserCategoryDTO category) {
        return Uni.createFrom()
            .item(authEntity())
            .flatMap(user -> categoryService.editCategory(user, key, category))
            .onItem().transform(UserCategoryDTO::fromCategory);
    }

    private UserEntity authEntity() {
        return (UserEntity) identity.getPrincipal();
    }
}
