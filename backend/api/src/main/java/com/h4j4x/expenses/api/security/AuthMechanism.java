package com.h4j4x.expenses.api.security;

import com.h4j4x.expenses.api.model.User;
import io.quarkus.arc.Priority;
import io.quarkus.security.identity.IdentityProviderManager;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.AuthenticationRequest;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.quarkus.smallrye.jwt.runtime.auth.JWTAuthMechanism;
import io.quarkus.vertx.http.runtime.security.ChallengeData;
import io.quarkus.vertx.http.runtime.security.HttpAuthenticationMechanism;
import io.quarkus.vertx.http.runtime.security.HttpCredentialTransport;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import java.util.Set;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;

@Alternative
@ApplicationScoped
@Priority(1)
public class AuthMechanism implements HttpAuthenticationMechanism {
    private final JWTAuthMechanism delegate;

    public AuthMechanism(JWTAuthMechanism delegate) {
        this.delegate = delegate;
    }

    @Override
    public Uni<SecurityIdentity> authenticate(RoutingContext context, IdentityProviderManager identityProviderManager) {
        return delegate.authenticate(context, identityProviderManager)
            .onItem().transform(identity -> {
                if (identity == null) {
                    return null;
                }
                var principal = new User();
                principal.setName("todo"); // todo: from data
                principal.setEmail(identity.getPrincipal().getName()); // todo: from data
                return QuarkusSecurityIdentity.builder()
                    .setPrincipal(principal)
                    .addAttributes(identity.getAttributes())
                    .addCredentials(identity.getCredentials())
                    .addRoles(identity.getRoles())
                    .setAnonymous(identity.isAnonymous())
                    .build();
            });
    }

    @Override
    public Uni<ChallengeData> getChallenge(RoutingContext context) {
        return delegate.getChallenge(context);
    }

    @Override
    public Set<Class<? extends AuthenticationRequest>> getCredentialTypes() {
        return delegate.getCredentialTypes();
    }

    @Override
    public Uni<Boolean> sendChallenge(RoutingContext context) {
        return delegate.sendChallenge(context);
    }

    @Override
    public Uni<HttpCredentialTransport> getCredentialTransport(RoutingContext context) {
        return delegate.getCredentialTransport(context);
    }

    @Override
    public int getPriority() {
        return 1;
    }
}
