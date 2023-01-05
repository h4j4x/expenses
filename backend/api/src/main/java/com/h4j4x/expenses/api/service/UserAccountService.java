package com.h4j4x.expenses.api.service;

import com.h4j4x.expenses.api.domain.UserAccount;
import com.h4j4x.expenses.api.domain.UserEntity;
import com.h4j4x.expenses.api.model.UserAccountDTO;
import com.h4j4x.expenses.api.repository.UserAccountRepository;
import io.smallrye.mutiny.Uni;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.BadRequestException;

@ApplicationScoped
public class UserAccountService {
    public static final String ACCOUNT_NAME_EXISTS_MESSAGE = "Account name already registered";

    private final UserAccountRepository accountRepo;

    public UserAccountService(UserAccountRepository accountRepo) {
        this.accountRepo = accountRepo;
    }

    public Uni<UserAccount> addAccount(UserEntity user, UserAccountDTO account) {
        return accountRepo.countByUserAndName(user, account.getName())
            .onItem().transform(count -> {
                if (count > 0) {
                    return null;
                }
                return count;
            })
            .onItem().ifNull().failWith(new BadRequestException(ACCOUNT_NAME_EXISTS_MESSAGE))
            .onItem().ifNotNull().transformToUni(c -> createAccount(user, account));
    }

    private Uni<UserAccount> createAccount(UserEntity user, UserAccountDTO account) {
        var userAccount = new UserAccount(user, account.getName());
        // todo: if balance > 0, add transaction
        return accountRepo.save(userAccount);
    }
}
