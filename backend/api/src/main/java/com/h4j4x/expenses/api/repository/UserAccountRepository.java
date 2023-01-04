package com.h4j4x.expenses.api.repository;

import com.h4j4x.expenses.api.domain.UserAccount;
import com.h4j4x.expenses.api.domain.UserEntity;
import io.smallrye.mutiny.Uni;
import java.util.Optional;
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

    public Uni<UserAccount> findByUserAndName(UserEntity user, String name) {
        if (user != null) {
            return find("user_id = ?1 and name = ?2", user.getId(), name).firstResult();
        }
        return Uni.createFrom().optional(Optional.empty());
    }

    public Uni<Long> countByUser(UserEntity user) {
        if (user != null) {
            return count("user_id", user.getId());
        }
        return Uni.createFrom().item(0L);
    }

    public Uni<Long> countByUserAndNameAndNotId(UserEntity user, String email, Long id) {
        if (user != null) {
            return count("user_id = ?1 and name = ?2 and id != ?3", user.getId(), email, id);
        }
        return Uni.createFrom().item(0L);
    }
}
