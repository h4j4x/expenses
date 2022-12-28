package com.h4j4x.expenses.api.resource;

import io.smallrye.jwt.build.Jwt;
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
    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String jwtIssuer;

    @POST
    @PermitAll
    @Path("/sign-in")
    public String signIn() { // todo: payload
        return Jwt.issuer(jwtIssuer)
            .upn("jdoe@quarkus.io") // todo: from user
            .groups("user")
            .sign();
    }

    @GET
    @RolesAllowed("user")
    @Path("/me")
    public String me(@Context SecurityContext securityContext) {
        return securityContext.getUserPrincipal().getName();
    }
}
