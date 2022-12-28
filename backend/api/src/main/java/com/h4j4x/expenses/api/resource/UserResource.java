package com.h4j4x.expenses.api.resource;

import com.h4j4x.expenses.api.model.Auth;
import io.smallrye.jwt.build.Jwt;
import io.smallrye.mutiny.Uni;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Path("/users")
public class UserResource {
    static final String USER_ROLE = "user";

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String jwtIssuer;

    @POST
    @PermitAll
    @Path("/sign-in")
    public Uni<String> signIn(Auth auth) {
        String token = Jwt.issuer(jwtIssuer)
            .upn(auth.getEmail())
            .groups(USER_ROLE)
            .sign();
        return Uni.createFrom().item(token);
    }

    @GET
    @RolesAllowed(USER_ROLE)
    @Path("/me")
    public Uni<String> me(@Context SecurityContext securityContext) {
        String name = securityContext.getUserPrincipal().getName();
        return Uni.createFrom().item(name);
    }
}
