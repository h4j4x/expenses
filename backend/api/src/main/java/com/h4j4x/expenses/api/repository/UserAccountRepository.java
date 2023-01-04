package com.h4j4x.expenses.api.repository;

import com.h4j4x.expenses.api.domain.UserAccount;
import io.smallrye.mutiny.Uni;
import javax.enterprise.context.ApplicationScoped;
import javax.validation.Validator;

@ApplicationScoped
public class UserAccountRepository extends BaseRepository<UserAccount> {
    private final Validator validator;

    public UserAccountRepository(Validator validator) {
        this.validator = validator;
    }

    public Uni<UserAccount> save(UserAccount account) {
        return super.save(account, validator);
    }
}
