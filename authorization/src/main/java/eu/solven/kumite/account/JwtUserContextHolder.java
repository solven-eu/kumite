package eu.solven.kumite.account;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import eu.solven.kumite.security.LoginRouteButNotAuthenticatedException;
import reactor.core.publisher.Mono;

public class JwtUserContextHolder implements IKumiteUserContextHolder {

	@Override
	public Mono<UUID> authenticatedAccountId() {
		return ReactiveSecurityContextHolder.getContext().map(securityContext -> {
			Authentication authentication = securityContext.getAuthentication();

			if (authentication instanceof JwtAuthenticationToken jwtAuth) {
				UUID accountId = UUID.fromString(jwtAuth.getToken().getSubject());

				return accountId;
			} else {
				throw new LoginRouteButNotAuthenticatedException("Expecting a JWT token");
			}
		});
	}

	@Override
	public Mono<UUID> authenticatedPlayerId() {
		return ReactiveSecurityContextHolder.getContext().map(securityContext -> {
			Authentication authentication = securityContext.getAuthentication();

			if (authentication instanceof JwtAuthenticationToken jwtAuth) {
				UUID accountId = UUID.fromString(jwtAuth.getToken().getClaimAsString("playerId"));

				return accountId;
			} else {
				throw new LoginRouteButNotAuthenticatedException("Expecting a JWT token");
			}
		});
	}

}
