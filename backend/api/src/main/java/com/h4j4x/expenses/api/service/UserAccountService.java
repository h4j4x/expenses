package com.h4j4x.expenses.api.service;

import com.h4j4x.expenses.api.domain.UserAccount;
import com.h4j4x.expenses.api.domain.UserEntity;
import com.h4j4x.expenses.api.domain.UserTransaction;
import com.h4j4x.expenses.api.model.*;
import com.h4j4x.expenses.api.repository.UserAccountRepository;
import com.h4j4x.expenses.common.util.NumberUtils;
import com.h4j4x.expenses.common.util.ObjectUtils;
import com.h4j4x.expenses.common.util.StringUtils;
import io.smallrye.mutiny.Uni;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
public class UserAccountService {
    public static final String ACCOUNT_NAME_EXISTS_MESSAGE = "Account name already registered";
    public static final String ACCOUNT_NOT_FOUND_MESSAGE = "Account not found";
    public static final String TRANSACTION_INITIAL_BALANCE_NOTES = "Initial balance";
    public static final String TRANSACTION_ADJUSTED_BALANCE_NOTES = "Adjusted balance";

    private final UserAccountRepository accountRepo;

    private final UserTransactionService transactionService;

    @ConfigProperty(name = "app.account.default-type", defaultValue = "MONEY")
    AccountType defaultAccountType;

    @ConfigProperty(name = "app.account.default-currency", defaultValue = "usd")
    String defaultCurrency;

    public UserAccountService(UserAccountRepository accountRepo, UserTransactionService transactionService) {
        this.accountRepo = accountRepo;
        this.transactionService = transactionService;
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
        userAccount.setAccountType(ObjectUtils.firstNotNull(account.getAccountType(), defaultAccountType));
        userAccount.setCurrency(StringUtils.firstNotBlank(account.getCurrency(), defaultCurrency));
        userAccount.setBalanceUpdatedAt(OffsetDateTime.now());
        return accountRepo.save(userAccount)
            .onItem().invoke(savedAccount -> {
                if (account.getBalanceDoubleValue() != 0) {
                    var transaction = new UserTransactionDTO(TRANSACTION_INITIAL_BALANCE_NOTES, account.getBalance());
                    transaction.setCreationWay(TransactionCreationWay.SYSTEM);
                    transaction.setStatus(TransactionStatus.CONFIRMED);
                    transactionService.addTransaction(savedAccount, transaction);
                }
            });
    }

    public Uni<List<UserAccount>> getAccounts(UserEntity user) {
        return accountRepo.findAllByUser(user);
    }

    public Uni<PageData<UserAccount>> getAccountsPaged(UserEntity user, Integer pageIndex, Integer pageSize) {
        return accountRepo.findPageByUser(user, pageIndex, pageSize);
    }

    public Uni<UserAccount> editAccount(UserEntity user, String key, UserAccountDTO account) {
        var userId = UserAccount.parseUserId(key);
        if (!user.getId().equals(userId)) {
            return Uni.createFrom().failure(new NotFoundException(ACCOUNT_NOT_FOUND_MESSAGE));
        }
        var id = UserAccount.parseAccountId(key);
        return accountRepo.findByUserAndId(user, id)
            .onItem().ifNull().failWith(new NotFoundException(ACCOUNT_NOT_FOUND_MESSAGE))
            .onItem().ifNotNull().call(userAccount -> accountRepo
                .countByUserAndNameAndNotId(user, account.getName(), userAccount.getId())
                .onItem().transform(count -> {
                    if (count > 0) {
                        return null;
                    }
                    return count;
                })
                .onItem().ifNull().failWith(new BadRequestException(ACCOUNT_NAME_EXISTS_MESSAGE)))
            .onItem().ifNotNull().transformToUni(userAccount -> {
                userAccount.setName(account.getName());
                userAccount.setAccountType(ObjectUtils
                    .firstNotNull(account.getAccountType(), userAccount.getAccountType()));
                userAccount.setCurrency(StringUtils
                    .firstNotBlank(account.getCurrency(), userAccount.getCurrency()));
                return accountRepo.save(userAccount)
                    .onItem().invoke(savedAccount -> {
                        if (account.getBalanceDoubleValue() != 0) {
                            var transaction = new UserTransactionDTO(TRANSACTION_ADJUSTED_BALANCE_NOTES, account.getBalance());
                            transaction.setCreationWay(TransactionCreationWay.SYSTEM);
                            transaction.setStatus(TransactionStatus.ADJUST_PENDING);
                            transactionService.addTransaction(savedAccount, transaction);
                        }
                    });
            });
    }

    @Incoming("user-account-transactions-in") // todo: test
    @Acknowledgment(Acknowledgment.Strategy.POST_PROCESSING)
    public void updateAccountBalance(String accountId) {
        Uni.createFrom().item(NumberUtils.parseLong(accountId))
            .onItem().ifNotNull().transformToUni(id -> accountRepo.findById(id))
            .onItem().ifNotNull().transformToUni(this::updateAccountBalance)
            .await().indefinitely();
    }

    public Uni<UserAccount> updateAccountBalance(UserAccount account) {
        var now = OffsetDateTime.now();
        return transactionService
            .findTransactionsFromDateWithStatus(account, account.getBalanceUpdatedAt(), TransactionStatus.CONFIRMED)
            .collect()
            .with(Collectors.summingDouble(UserTransaction::getAmount))
            .onItem().transformToUni(balance -> {
                account.setBalance(balance);
                account.setBalanceUpdatedAt(now);
                return accountRepo.save(account);
            });
    }
}
