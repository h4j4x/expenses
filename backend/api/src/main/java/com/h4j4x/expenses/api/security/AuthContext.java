package com.h4j4x.expenses.api.security;

import com.h4j4x.expenses.api.domain.UserEntity;
import javax.enterprise.context.RequestScoped;

@RequestScoped
public class AuthContext {
    private UserEntity authUser;

    public UserEntity getAuthUser() {
        return authUser;
    }

    public void setAuthUser(UserEntity authUser) {
        this.authUser = authUser;
    }
}
