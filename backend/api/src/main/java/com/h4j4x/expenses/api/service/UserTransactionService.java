package com.h4j4x.expenses.api.service;

import com.h4j4x.expenses.api.domain.UserAccount;
import com.h4j4x.expenses.api.domain.UserTransaction;
import com.h4j4x.expenses.api.model.TransactionCreationWay;
import com.h4j4x.expenses.api.model.TransactionStatus;
import com.h4j4x.expenses.api.model.UserTransactionDTO;
import com.h4j4x.expenses.api.repository.UserTransactionRepository;
import com.h4j4x.expenses.common.util.ObjectUtils;
import io.smallrye.mutiny.Uni;
import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class UserTransactionService {
    private final UserTransactionRepository transactionRepo;

    @ConfigProperty(name = "app.account.default-creation-way", defaultValue = "MANUAL")
    TransactionCreationWay defaultTransactionCreationWay;

    @ConfigProperty(name = "app.transaction.default-status", defaultValue = "PENDING")
    TransactionStatus defaultTransactionStatus;

    public UserTransactionService(UserTransactionRepository transactionRepo) {
        this.transactionRepo = transactionRepo;
    }

    public Uni<UserTransaction> addTransaction(UserAccount account, UserTransactionDTO transaction) {
        var userTransaction = new UserTransaction(account, transaction.getNotes(), transaction.getAmount());
        userTransaction.setCreationWay(ObjectUtils.firstNotNull(transaction.getCreationWay(), defaultTransactionCreationWay));
        userTransaction.setStatus(ObjectUtils.firstNotNull(transaction.getStatus(), defaultTransactionStatus));
        return transactionRepo.save(userTransaction)
            .onItem().invoke(savedTransaction -> {
                // todo: notify queue to update account balance
            });
    }
}
