package com.h4j4x.expenses.api.client;

import com.h4j4x.expenses.api.model.UserDTO;
import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Name;

@GraphQLClientApi(configKey = "graphql")
public interface UserResourceClient {
    @Mutation
    Uni<UserDTO> editUser(@Name("user") UserDTO user);
}
