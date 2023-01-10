package com.h4j4x.expenses.api.repository;

import com.h4j4x.expenses.api.domain.UserTransaction;
import io.smallrye.mutiny.Uni;
import javax.enterprise.context.ApplicationScoped;
import javax.validation.Validator;

@ApplicationScoped
public class UserTransactionRepository extends BaseRepository<UserTransaction> {
    private final Validator validator;

    public UserTransactionRepository(Validator validator) {
        this.validator = validator;
    }

    public Uni<UserTransaction> save(UserTransaction transaction) {
        return super.save(transaction, validator);
    }
}
