package com.h4j4x.expenses.api.service;

import com.h4j4x.expenses.api.domain.UserAccount;
import com.h4j4x.expenses.api.domain.UserEntity;
import com.h4j4x.expenses.api.model.AccountType;
import com.h4j4x.expenses.api.model.PageData;
import com.h4j4x.expenses.api.model.UserAccountDTO;
import com.h4j4x.expenses.api.repository.UserAccountRepository;
import com.h4j4x.expenses.common.util.ObjectUtils;
import com.h4j4x.expenses.common.util.StringUtils;
import io.smallrye.mutiny.Uni;
import java.time.OffsetDateTime;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class UserAccountService {
    public static final String ACCOUNT_NAME_EXISTS_MESSAGE = "Account name already registered";
    public static final String ACCOUNT_NOT_FOUND_MESSAGE = "Account not found";

    private final UserAccountRepository accountRepo;

    @ConfigProperty(name = "app.account.default-type", defaultValue = "MONEY")
    AccountType defaultAccountType;

    @ConfigProperty(name = "app.account.default-currency", defaultValue = "usd")
    String defaultCurrency;

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
        userAccount.setAccountType(ObjectUtils.firstNotNull(account.getAccountType(), defaultAccountType));
        userAccount.setCurrency(StringUtils.firstNotBlank(account.getCurrency(), defaultCurrency));
        userAccount.setBalanceUpdatedAt(OffsetDateTime.now());
        // todo: if balance > 0, add transaction
        return accountRepo.save(userAccount);
    }

    public Uni<List<UserAccount>> getAccounts(UserEntity user) {
        return accountRepo.findAllByUser(user);
    }

    public Uni<PageData<UserAccount>> getAccountsPaged(UserEntity user, Integer pageIndex, Integer pageSize) {
        return accountRepo.findPageByUser(user, pageIndex, pageSize);
    }

    public Uni<UserAccount> editAccount(UserEntity user, String key, UserAccountDTO account) {
        Long userId = UserAccount.parseUserId(key);
        if (!user.getId().equals(userId)) {
            return Uni.createFrom().failure(new NotFoundException(ACCOUNT_NOT_FOUND_MESSAGE));
        }
        Long id = UserAccount.parseAccountId(key);
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
                // todo: if current balance != new balance, add transaction to adjust
                return accountRepo.save(userAccount);
            });
    }
}
