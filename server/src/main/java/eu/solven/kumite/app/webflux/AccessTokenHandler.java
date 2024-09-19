package eu.solven.kumite.app.webflux;

import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import eu.solven.kumite.account.KumiteUser;
import eu.solven.kumite.account.login.KumiteTokenService;
import eu.solven.kumite.app.controllers.KumiteHandlerHelper;
import eu.solven.kumite.login.AccessTokenHolder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Slf4j
public class AccessTokenHandler {

	final KumiteTokenService kumiteTokenService;

	public Mono<ServerResponse> getAccessToken(ServerRequest request) {
		KumiteUser user = null;
		UUID queryPlayerId = KumiteHandlerHelper.uuid(request, "player_id");

		return ReactiveSecurityContextHolder.getContext().map(securityContext -> {
			log.info("2We need to check if playerId={} is valid given JWT={}",
					queryPlayerId,
					securityContext.getAuthentication());

			return securityContext.getAuthentication();
		}).flatMap(auth2 -> {
			AccessTokenHolder tokenWrapper = kumiteTokenService.wrapInJwtToken(user, queryPlayerId);

			return ServerResponse.ok()
					.contentType(MediaType.APPLICATION_JSON)
					.body(BodyInserters.fromValue(tokenWrapper));
		});

	}

}
