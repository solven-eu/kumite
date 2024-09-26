package eu.solven.kumite.app.webflux.api;

import static org.springdoc.core.fn.builders.apiresponse.Builder.responseBuilder;
import static org.springdoc.core.fn.builders.parameter.Builder.parameterBuilder;

import org.springdoc.core.fn.builders.parameter.Builder;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import eu.solven.kumite.app.webflux.PlayerVerifierFilterFunction;
import eu.solven.kumite.login.AccessTokenWrapper;
import lombok.extern.slf4j.Slf4j;

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
	public RouterFunction<ServerResponse> loginRoutes(PlayerVerifierFilterFunction playerVerifierFilterFunction,
			GreetingHandler greetingHandler,
			AccessTokenHandler accessTokenHandler) {
		Builder playerId = parameterBuilder().name("player_id").description("Search for a specific playerId");

		return SpringdocRouteBuilder.route()

				// These API are useful only to test the API
				.GET(json("/hello"),
						greetingHandler::hello,
						ops -> ops.operationId("hello").response(responseBuilder().implementation(Greeting.class)))
				.POST(json("/hello"),
						greetingHandler::hello,
						ops -> ops.operationId("hello").response(responseBuilder().implementation(Greeting.class)))

				// https://datatracker.ietf.org/doc/html/rfc6749#section-1.5
				// https://curity.io/resources/learn/oauth-refresh/
				// `/token` is the standard route to fetch tokens
				.GET(json("/oauth2/token"),
						accessTokenHandler::getAccessToken,
						ops -> ops.operationId("getAccessTokenFromRefreshToken")
								.parameter(playerId)
								.response(responseBuilder().implementation(AccessTokenWrapper.class)))

				.filter(playerVerifierFilterFunction, ops -> {
					// https://github.com/springdoc/springdoc-openapi/issues/1538
					ops.operationId("login");
				})

				.build();

	}
}