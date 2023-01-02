package com.h4j4x.expenses.api.resource;

import com.h4j4x.expenses.api.domain.UserEntity;
import com.h4j4x.expenses.api.model.UserDTO;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import javax.inject.Inject;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class UserResource {
    @Inject
    SecurityIdentity identity;

    @Query
    @Description("Get authenticated user")
    public Uni<UserDTO> getUser() {
        return Uni.createFrom()
            .item(UserDTO.fromEntity((UserEntity) identity.getPrincipal()));
    }
}
