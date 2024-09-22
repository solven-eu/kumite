package eu.solven.kumite.app.webflux;

import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import eu.solven.kumite.account.KumiteUser;
import eu.solven.kumite.account.login.KumiteUsersRegistry;
import eu.solven.kumite.app.controllers.KumiteHandlerHelper;
import eu.solven.kumite.login.AccessTokenWrapper;
import eu.solven.kumite.oauth2.authorizationserver.KumiteTokenService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Slf4j
public class AccessTokenHandler {

	final KumiteTokenService kumiteTokenService;
	final KumiteUsersRegistry usersRegistry;

	// This route has to be authenticated with a refresh_token as access_token. This is not standard following OAuth2,
	// but to do it clean, we would need any way to provide a separate Authentication Server.
	public Mono<ServerResponse> getAccessToken(ServerRequest request) {
		// The playerId authenticated by the accessToken
		UUID queryPlayerId = KumiteHandlerHelper.uuid(request, "player_id");

		return ReactiveSecurityContextHolder.getContext().map(securityContext -> {
			Authentication authentication = securityContext.getAuthentication();

			KumiteUser user = userFromRefreshTokenJwt(authentication);

			return user;
		}).flatMap(user -> {
			AccessTokenWrapper tokenWrapper = kumiteTokenService.wrapInJwtAccessToken(user, queryPlayerId);

			return ServerResponse.ok()
					.contentType(MediaType.APPLICATION_JSON)
					.body(BodyInserters.fromValue(tokenWrapper));
		});
	}

	private KumiteUser userFromRefreshTokenJwt(Authentication authentication) {
		JwtAuthenticationToken jwtAuthentication = (JwtAuthenticationToken) authentication;

		Jwt jwt = jwtAuthentication.getToken();

		if (!jwt.getClaimAsBoolean("refresh_token")) {
			throw new IllegalArgumentException("Authenticate yourself with a refresh_token, not an access_token");
		}

		UUID accountId = UUID.fromString(jwt.getSubject());

		KumiteUser user = usersRegistry.getUser(accountId);
		log.debug("We loaded {} from jti={}", user, jwt.getId());
		return user;
	}

}
