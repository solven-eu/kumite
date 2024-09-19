package eu.solven.kumite.app.webflux;

import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;

import java.util.Optional;
import java.util.UUID;

import org.springdoc.core.fn.builders.parameter.Builder;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import eu.solven.kumite.app.controllers.KumiteHandlerHelper;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Redirect each route (e.g. `/games/someGameId`) to the appropriate handler.
 * 
 * @author Benoit Lacelle
 *
 */
@Configuration(proxyBeanMethods = false)
@Slf4j
public class KumiteLoginRouter {

	private static final RequestPredicate json(String path) {
		final RequestPredicate json = RequestPredicates.accept(MediaType.APPLICATION_JSON);
		return RequestPredicates.path("/api/v1" + path).and(json);
	}

	@Bean
	public RouterFunction<ServerResponse> loginRoutes(AccessTokenHandler accessTokenHandler) {
		Builder playerId = parameterBuilder().name("player_id").description("Search for a specific playerId");

		return SpringdocRouteBuilder.route()

				.GET(json("/token"),
						accessTokenHandler::getAccessToken,
						ops -> ops.operationId("getLongLivesAccessToken").parameter(playerId))

				// Activate webhooks later. For now, we focus on long-polling
				// .GET(json("/webhooks"),
				// webhooksHandler::listWebhooks,
				// ops -> ops.operationId("listWebhooks"))
				// .PUT(RequestPredicates.PUT("/webhooks"),
				// webhooksHandler::registerWebhook,
				// ops -> ops.operationId("publishWebhook"))
				// .DELETE(RequestPredicates.DELETE("/webhooks"),
				// webhooksHandler::dropWebhooks,
				// ops -> ops.operationId("deleteWebhook"))

				.filter((request, next) -> {
					Optional<UUID> optPlayerId = KumiteHandlerHelper.optUuid(request, "player_id");

					return Mono.justOrEmpty(optPlayerId).map(queryPlayerId -> {
						Authentication auth = SecurityContextHolder.getContext().getAuthentication();
						log.debug("1We need to check if playerId={} is valid given JWT={}", queryPlayerId, auth);

						return ReactiveSecurityContextHolder.getContext().map(securityContext -> {
							log.debug("2We need to check if playerId={} is valid given JWT={}",
									queryPlayerId,
									securityContext.getAuthentication());

							return Mono.empty();
						});
					}).then(next.handle(request));
				}, ops -> {
				})
				.build();

	}
}