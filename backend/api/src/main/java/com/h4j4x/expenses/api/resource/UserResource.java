package com.h4j4x.expenses.api.resource;

import com.h4j4x.expenses.api.model.Auth;
import com.h4j4x.expenses.api.model.User;
import io.quarkus.security.Authenticated;
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

@Path("/users")
public class UserResource {
    static final String SIGN_IN = "sign-in";
    static final String ME = "me";

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String jwtIssuer;

    @ConfigProperty(name = "app.auth.token-expiration-in-days", defaultValue = "30")
    Integer tokenExpirationInDays;

    @POST
    @PermitAll
    @Path("/" + SIGN_IN)
    public Uni<String> signIn(Auth auth) {
        var token = Jwt.issuer(jwtIssuer)
            .upn(auth.getEmail())
            .groups("user") // todo: from data
            .expiresIn(Duration.ofDays(tokenExpirationInDays))
            .sign();
        return Uni.createFrom().item(token);
    }

    @GET
    @Authenticated
    @Path("/" + ME)
    public Uni<User> me(@Context SecurityContext securityContext) {
        return Uni.createFrom().item((User) securityContext.getUserPrincipal());
    }
}
