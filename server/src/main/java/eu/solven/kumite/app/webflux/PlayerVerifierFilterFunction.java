package eu.solven.kumite.app.webflux;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import eu.solven.kumite.app.webflux.api.KumiteHandlerHelper;
import eu.solven.kumite.security.LoginRouteButNotAuthenticatedException;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * This checks than any route relying on a `player_id` parameter matches the playerId of the JWT.
 * 
 * @author Benoit Lacelle
 *
 */
@Slf4j
public class PlayerVerifierFilterFunction implements HandlerFilterFunction<ServerResponse, ServerResponse> {

	@Override
	public @NonNull Mono<ServerResponse> filter(@NonNull ServerRequest request,
			@NonNull HandlerFunction<ServerResponse> next) {
		Optional<UUID> optPlayerId = KumiteHandlerHelper.optUuid(request, "player_id");

		return ReactiveSecurityContextHolder.getContext()
				.switchIfEmpty(Mono.error(() -> new LoginRouteButNotAuthenticatedException("Lack of authorization")))
				.map(securityContext -> {
					Authentication authentication = securityContext.getAuthentication();

					optPlayerId.ifPresent(queryPlayerId -> {

						if (authentication instanceof JwtAuthenticationToken jwtAuth) {
							List<String> rawPlayerIds = jwtAuth.getToken().getClaimAsStringList("playerIds");
							Set<UUID> jwtPlayerIds =
									rawPlayerIds.stream().map(UUID::fromString).collect(Collectors.toSet());

							if (!jwtPlayerIds.contains(queryPlayerId)) {
								throw new IllegalAccessError(
										"playerId=" + queryPlayerId + " is not amongst JWT playerIds: " + jwtPlayerIds);
							} else {
								log.debug("Accepted playerId={} given JWT.playerIds={}", queryPlayerId, jwtPlayerIds);
							}
						} else {
							throw new LoginRouteButNotAuthenticatedException("Expecting a JWT token");
						}
					});

					return authentication;
				})
				.then(next.handle(request));
	}
}
