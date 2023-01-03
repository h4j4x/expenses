package com.h4j4x.expenses.api.resource;

import com.h4j4x.expenses.api.domain.UserEntity;
import com.h4j4x.expenses.api.model.UserDTO;
import com.h4j4x.expenses.api.service.UserService;
import io.quarkus.hibernate.reactive.panache.common.runtime.ReactiveTransactional;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
@ReactiveTransactional
public class UserResource {
    private final SecurityIdentity identity;

    private final UserService userService;

    public UserResource(SecurityIdentity identity, UserService userService) {
        this.identity = identity;
        this.userService = userService;
    }

    @Query
    @Description("Get authenticated user")
    public Uni<UserDTO> getUser() {
        return Uni.createFrom()
            .item(authEntity())
            .onItem().transform(UserDTO::fromEntity);
    }

    @Mutation
    @Description("Edit authenticated user")
    public Uni<UserDTO> editUser(UserDTO user) {
        return Uni.createFrom()
            .item(authEntity())
            .flatMap(userEntity -> userService.editUser(userEntity, user))
            .onItem().transform(UserDTO::fromEntity);
    }

    private UserEntity authEntity() {
        return (UserEntity) identity.getPrincipal();
    }
}
