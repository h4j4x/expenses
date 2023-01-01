package com.h4j4x.expenses.api.resource;

import com.h4j4x.expenses.api.model.UserDTO;
import com.h4j4x.expenses.api.security.AuthContext;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class UserResource {
    private final AuthContext authContext;

    public UserResource(AuthContext authContext) {
        this.authContext = authContext;
    }

    @Query
    @Description("Get authenticated user")
    public Uni<UserDTO> getUser() {
        return Uni.createFrom()
            .item(UserDTO.fromEntity(authContext.getAuthUser()));
    }
}
