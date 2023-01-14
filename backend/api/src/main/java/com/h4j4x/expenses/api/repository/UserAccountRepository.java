package com.h4j4x.expenses.api.repository;

import com.h4j4x.expenses.api.domain.UserAccount;
import com.h4j4x.expenses.api.domain.UserEntity;
import com.h4j4x.expenses.api.model.PageData;
import io.smallrye.mutiny.Uni;
import java.util.Collections;
import java.util.List;
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

    public Uni<UserAccount> findByUserAndId(UserEntity user, Long id) {
        if (user != null) {
            return find("user_id = ?1 and id = ?2", user.getId(), id).firstResult();
        }
        return Uni.createFrom().optional(Optional.empty());
    }

    public Uni<Long> countByUser(UserEntity user) {
        if (user != null) {
            return count("user_id", user.getId());
        }
        return Uni.createFrom().item(0L);
    }

    public Uni<List<UserAccount>> findAllByUser(UserEntity user) {
        if (user != null) {
            return find("user_id", user.getId()).list();
        }
        return Uni.createFrom().item(Collections.emptyList());
    }

    public Uni<PageData<UserAccount>> findPageByUser(UserEntity user, int pageIndex, int pageSize) {
        if (user != null) {
            var query = find("user_id", user.getId());
            return Uni.combine()
                .all().unis(query.page(pageIndex, pageSize).list(), query.count()).asTuple()
                .onItem().transform(tuple -> new PageData<>(tuple.getItem1(), pageIndex, pageSize, tuple.getItem2()));
        }
        return Uni.createFrom().item(PageData.empty());
    }

    public Uni<Long> countByUserAndName(UserEntity user, String name) {
        if (user != null) {
            return count("user_id = ?1 and name = ?2", user.getId(), name);
        }
        return Uni.createFrom().item(0L);
    }

    public Uni<Long> countByUserAndNameAndNotId(UserEntity user, String name, Long id) {
        if (user != null) {
            return count("user_id = ?1 and name = ?2 and id != ?3", user.getId(), name, id);
        }
        return Uni.createFrom().item(0L);
    }
}
