package com.h4j4x.expenses.api.model;

public record UserToken(String token, Long expiresInHours) {
}
