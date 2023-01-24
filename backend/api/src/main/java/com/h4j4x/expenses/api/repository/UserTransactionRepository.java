package com.h4j4x.expenses.api.repository;

import com.h4j4x.expenses.api.domain.UserAccount;
import com.h4j4x.expenses.api.domain.UserTransaction;
import com.h4j4x.expenses.api.model.TransactionStatus;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import java.time.OffsetDateTime;
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

    public Multi<UserTransaction> findTransactionsFromDateWithStatus(UserAccount account,
                                                                     OffsetDateTime from,
                                                                     TransactionStatus status) {
        return find("account_id = ?1 and createdAt >= ?2 and status = ?3", account.getId(), from, status)
            .stream();
    }
}
