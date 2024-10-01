package eu.solven.kumite.app.webflux.api;

import java.text.ParseException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.nimbusds.jwt.SignedJWT;

import eu.solven.kumite.account.KumiteUsersRegistry;
import eu.solven.kumite.account.internal.KumiteUser;
import eu.solven.kumite.login.AccessTokenWrapper;
import eu.solven.kumite.oauth2.authorizationserver.ActiveRefreshTokens;
import eu.solven.kumite.oauth2.authorizationserver.KumiteTokenService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Slf4j
public class AccessTokenHandler {

	final KumiteTokenService kumiteTokenService;
	final KumiteUsersRegistry usersRegistry;

	final ActiveRefreshTokens activeRefreshTokens;

	// This route has to be authenticated with a refresh_token as access_token. This is not standard following OAuth2,
	// but to do it clean, we would need any way to provide a separate Authentication Server.
	public Mono<ServerResponse> getAccessToken(ServerRequest request) {
		// The playerId authenticated by the accessToken
		UUID queryPlayerId = KumiteHandlerHelper.uuid(request, "player_id");

		return ReactiveSecurityContextHolder.getContext().flatMap(securityContext -> {
			Authentication authentication = securityContext.getAuthentication();

			Entry<Jwt, KumiteUser> user = userFromRefreshTokenJwt(authentication);

			AccessTokenWrapper tokenWrapper =
					kumiteTokenService.wrapInJwtAccessToken(KumiteUser.raw(user.getValue()), queryPlayerId);

			String accessTokenJti = getJti(tokenWrapper);

			log.info("playerId={} Generating access_token.kid={} given refresh_token.kid={}",
					queryPlayerId,
					accessTokenJti,
					user.getKey().getId());

			return ServerResponse.ok()
					.contentType(MediaType.APPLICATION_JSON)
					.body(BodyInserters.fromValue(tokenWrapper));
		});
	}

	private String getJti(AccessTokenWrapper tokenWrapper) {
		try {
			return SignedJWT.parse(tokenWrapper.getAccessToken()).getJWTClaimsSet().getJWTID();
		} catch (ParseException e) {
			throw new IllegalStateException("Issue parsing our own access_token", e);
		}
	}

	private Map.Entry<Jwt, KumiteUser> userFromRefreshTokenJwt(Authentication authentication) {
		JwtAuthenticationToken jwtAuthentication = (JwtAuthenticationToken) authentication;

		Jwt jwt = jwtAuthentication.getToken();

		if (!jwt.getClaimAsBoolean("refresh_token")) {
			throw new IllegalArgumentException("Authenticate yourself with a refresh_token, not an access_token");
		}

		UUID accountId = UUID.fromString(jwt.getSubject());

		activeRefreshTokens.touchRefreshToken(accountId, UUID.fromString(jwt.getId()));

		KumiteUser user = usersRegistry.getUser(accountId);
		log.debug("We loaded {} from jti={}", user, jwt.getId());
		return Map.entry(jwt, user);
	}

}
