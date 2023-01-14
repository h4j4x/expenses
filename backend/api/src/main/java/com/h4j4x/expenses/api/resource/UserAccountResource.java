package com.h4j4x.expenses.api.resource;

import com.h4j4x.expenses.api.domain.UserEntity;
import com.h4j4x.expenses.api.model.PageData;
import com.h4j4x.expenses.api.model.UserAccountDTO;
import com.h4j4x.expenses.api.service.UserAccountService;
import io.quarkus.hibernate.reactive.panache.common.runtime.ReactiveTransactional;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.mutiny.Uni;
import java.util.List;
import org.eclipse.microprofile.graphql.*;

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

    @Query
    @Description("Get user accounts")
    public Uni<List<UserAccountDTO>> getUserAccounts() {
        return accountService.getAccounts(authEntity())
            .map(userAccounts -> userAccounts.stream()
                .map(UserAccountDTO::fromAccount).toList());
    }

    @Query
    @Description("Get user accounts paged")
    public Uni<PageData<UserAccountDTO>> getUserPageAccounts(@DefaultValue("0") int pageIndex,
                                                             @DefaultValue("10") int pageSize) {
        return accountService.getAccountsPaged(authEntity(), pageIndex, pageSize)
            .map(page -> page.map(UserAccountDTO::fromAccount));
    }

    @Mutation
    @Description("Edit user account")
    public Uni<UserAccountDTO> editUserAccount(String key, UserAccountDTO account) {
        return Uni.createFrom()
            .item(authEntity())
            .flatMap(user -> accountService.editAccount(user, key, account))
            .onItem().transform(UserAccountDTO::fromAccount);
    }

    private UserEntity authEntity() {
        return (UserEntity) identity.getPrincipal();
    }
}
