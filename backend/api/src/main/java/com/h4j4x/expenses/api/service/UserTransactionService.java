package com.h4j4x.expenses.api.service;

import com.h4j4x.expenses.api.domain.UserAccount;
import com.h4j4x.expenses.api.domain.UserTransaction;
import com.h4j4x.expenses.api.model.TransactionCreationWay;
import com.h4j4x.expenses.api.model.TransactionStatus;
import com.h4j4x.expenses.api.model.UserTransactionDTO;
import com.h4j4x.expenses.api.repository.UserTransactionRepository;
import com.h4j4x.expenses.common.util.ObjectUtils;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import java.time.OffsetDateTime;
import javax.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@ApplicationScoped
public class UserTransactionService {
    private final UserTransactionRepository transactionRepo;

    @ConfigProperty(name = "app.account.default-creation-way", defaultValue = "MANUAL")
    TransactionCreationWay defaultTransactionCreationWay;

    @ConfigProperty(name = "app.transaction.default-status", defaultValue = "PENDING")
    TransactionStatus defaultTransactionStatus;

    @Channel("user-account-transactions-out")
    Emitter<String> eventEmitter;

    public UserTransactionService(UserTransactionRepository transactionRepo) {
        this.transactionRepo = transactionRepo;
    }

    public Uni<UserTransaction> addTransaction(UserAccount account, UserTransactionDTO transaction) {
        var userTransaction = new UserTransaction(account, transaction.getNotes(), transaction.getAmount());
        userTransaction.setCreationWay(ObjectUtils.firstNotNull(transaction.getCreationWay(), defaultTransactionCreationWay));
        userTransaction.setStatus(ObjectUtils.firstNotNull(transaction.getStatus(), defaultTransactionStatus));
        return transactionRepo.save(userTransaction)
            .onItem().invoke(savedTransaction -> eventEmitter.send(account.getId().toString()));
    }

    public Multi<UserTransaction> findTransactionsFromDateWithStatus(UserAccount account,
                                                                     OffsetDateTime from,
                                                                     TransactionStatus status) {
        return transactionRepo.findTransactionsFromDateWithStatus(account, from, status);
    }
}
