package com.h4j4x.expenses.api.resource;

import com.h4j4x.expenses.api.domain.UserEntity;
import com.h4j4x.expenses.api.model.UserDTO;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class UserResource {
    @Query
    @Description("Get authenticated user")
    public Uni<UserDTO> getUser() {
        var userEntity = new UserEntity("name", "email", "password"); // UserEntity) securityContext.getUserPrincipal();
        return Uni.createFrom()
            .item(UserDTO.fromEntity(userEntity));
    }
}
