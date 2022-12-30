package com.h4j4x.expenses.api.resource;

import com.h4j4x.expenses.api.domain.UserEntity;
import com.h4j4x.expenses.api.model.UserCredentials;
import com.h4j4x.expenses.api.model.UserDTO;
import com.h4j4x.expenses.api.service.UserService;
import io.quarkus.security.Authenticated;
import io.quarkus.security.AuthenticationFailedException;
import io.smallrye.jwt.build.Jwt;
import io.smallrye.mutiny.Uni;
import java.time.Duration;
import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.ResponseStatus;

@Path("/users")
public class UserResource {
    static final String SIGN_UP = "sign-up";
    static final String SIGN_IN = "sign-in";
    static final String ME = "me";

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String jwtIssuer;

    @ConfigProperty(name = "app.auth.token-expiration-in-days", defaultValue = "30")
    Integer tokenExpirationInDays;

    private final UserService userService;

    public UserResource(UserService userService) {
        this.userService = userService;
    }

    @POST
    @ResponseStatus(201)
    @PermitAll
    @Path("/" + SIGN_UP)
    public Uni<String> signUp(UserDTO userData) {
        return userService
            .createUser(userData.getName(), userData.getEmail(), userData.getPassword())
            .onItem().ifNotNull().transform(this::createToken);
    }

    @POST
    @PermitAll
    @Path("/" + SIGN_IN)
    public Uni<String> signIn(UserCredentials userCredentials) {
        return userService
            .findUserByEmailAndPassword(userCredentials.getEmail(), userCredentials.getPassword())
            .onItem().ifNotNull().transform(this::createToken)
            .onItem().ifNull().failWith(new AuthenticationFailedException("Invalid credentials"));
    }

    private String createToken(UserEntity userEntity) {
        return Jwt.issuer(jwtIssuer)
            .upn(userEntity.getEmail())
            .expiresIn(Duration.ofDays(tokenExpirationInDays))
            .sign();
    }

    @GET
    @Authenticated
    @Path("/" + ME)
    public Uni<UserDTO> me(@Context SecurityContext securityContext) {
        return Uni.createFrom().item((UserDTO) securityContext.getUserPrincipal());
    }
}
