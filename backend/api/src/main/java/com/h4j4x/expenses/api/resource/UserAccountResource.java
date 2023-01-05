package com.h4j4x.expenses.api.resource;

import com.h4j4x.expenses.api.domain.UserEntity;
import com.h4j4x.expenses.api.model.UserAccountDTO;
import com.h4j4x.expenses.api.service.UserAccountService;
import io.quarkus.hibernate.reactive.panache.common.runtime.ReactiveTransactional;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;

@GraphQLApi
@ReactiveTransactional
public class UserAccountResource {
    private final SecurityIdentity identity;

    private final UserAccountService accountService;

    public UserAccountResource(SecurityIdentity identity, UserAccountService accountService) {
        this.identity = identity;
        this.accountService = accountService;
    }

    @Mutation
    @Description("Add user account")
    public Uni<UserAccountDTO> addUserAccount(UserAccountDTO account) {
        return Uni.createFrom()
            .item(authEntity())
            .flatMap(userEntity -> accountService.addAccount(userEntity, account))
            .onItem().transform(UserAccountDTO::fromAccount);
    }

    private UserEntity authEntity() {
        return (UserEntity) identity.getPrincipal();
    }
}
