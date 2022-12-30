package com.h4j4x.expenses.api.resource;

import com.h4j4x.expenses.api.domain.UserEntity;
import com.h4j4x.expenses.api.model.UserCredentials;
import com.h4j4x.expenses.api.model.UserDTO;
import com.h4j4x.expenses.api.model.UserToken;
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
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.ResponseStatus;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
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
    public Uni<UserToken> signUp(UserDTO userDTO) {
        return userService
            .createUser(userDTO)
            .onItem().ifNotNull().transform(this::createToken);
    }

    @POST
    @PermitAll
    @Path("/" + SIGN_IN)
    public Uni<UserToken> signIn(UserCredentials userCredentials) {
        return userService
            .findUserByEmailAndPassword(userCredentials)
            .onItem().ifNotNull().transform(this::createToken)
            .onItem().ifNull().failWith(new AuthenticationFailedException("Invalid credentials"));
    }

    private UserToken createToken(UserEntity userEntity) {
        var expiresIn = Duration.ofDays(tokenExpirationInDays);
        var token = Jwt.issuer(jwtIssuer)
            .upn(userEntity.getEmail())
            .expiresIn(expiresIn)
            .sign();
        return new UserToken(token, expiresIn.toHours());
    }

    @GET
    @Authenticated
    @Path("/" + ME)
    public Uni<UserDTO> me(@Context SecurityContext securityContext) {
        var userEntity = (UserEntity) securityContext.getUserPrincipal();
        return Uni.createFrom()
            .item(UserDTO.fromEntity(userEntity));
    }
}
