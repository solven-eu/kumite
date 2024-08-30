package eu.solven.kumite.app.webflux;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import eu.solven.kumite.app.greeting.GreetingHandler;
import eu.solven.kumite.contest.ContestSearchHandler;
import eu.solven.kumite.game.GameSearchHandler;
import eu.solven.kumite.leaderboard.LeaderboardHandler;
import eu.solven.kumite.player.PlayersSearchHandler;
import eu.solven.kumite.webhook.WebhooksHandler;
import reactor.core.publisher.Mono;

@Configuration(proxyBeanMethods = false)
public class KumiteRouter {

	@Value("classpath:/static/index.html")
	private Resource indexHtml;

	@Bean
	public RouterFunction<ServerResponse> route(GreetingHandler greetingHandler,
			GameSearchHandler gamesSearchHandler,
			PlayersSearchHandler playersSearchHandler,
			ContestSearchHandler contestSearchHandler,
			LeaderboardHandler leaderboardHandler,
			WebhooksHandler webhooksHandler) {
		RequestPredicate json = RequestPredicates.accept(MediaType.APPLICATION_JSON);

		return RouterFunctions.route(RequestPredicates.GET("/api/hello").and(json), greetingHandler::hello)
				.and(RouterFunctions.route(RequestPredicates.GET("/api/games").and(json),
						gamesSearchHandler::listGames))

				.and(RouterFunctions.route(RequestPredicates.GET("/api/players").and(json),
						playersSearchHandler::listPlayers))

				.and(RouterFunctions.route(RequestPredicates.GET("/api/contests").and(json),
						contestSearchHandler::listContests))
				.and(RouterFunctions.route(RequestPredicates.PUT("/api/contests").and(json),
						contestSearchHandler::generateContest))

				.and(RouterFunctions.route(RequestPredicates.GET("/api/leaderboards").and(json),
						leaderboardHandler::listScores))

				.and(RouterFunctions.route(RequestPredicates.GET("/api/webhooks").and(json),
						webhooksHandler::listWebhooks))
				.and(RouterFunctions.route(RequestPredicates.PUT("/api/webhooks").and(json),
						webhooksHandler::registerWebhook))
				.and(RouterFunctions.route(RequestPredicates.DELETE("/api/webhooks").and(json),
						webhooksHandler::dropWebhooks))

				// ServerResponse.temporaryRedirect(URI.create("/")).build(indexHtml)
				.and(RouterFunctions.route(
						RequestPredicates.GET("/games/**").and(RequestPredicates.accept(MediaType.TEXT_HTML)),
						request -> ServerResponse.ok().contentType(MediaType.TEXT_HTML).bodyValue(indexHtml)));
	}

	// https://stackoverflow.com/questions/61822027/how-to-forward-to-on-404-error-in-webflux
	@Order(-2)
	@Component
	public class ResourceNotFoundRedirectWebFilter implements WebFilter {
		@Override
		public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
			if (exchange.getResponse().getStatusCode() == HttpStatus.NOT_FOUND) {
				exchange.getResponse().setStatusCode(HttpStatus.PERMANENT_REDIRECT);
				exchange.getResponse().getHeaders().setLocation(URI.create("/"));
				return exchange.getResponse().setComplete();
			}
			return chain.filter(exchange);
		}
	}

	@Bean
	public ResourceNotFoundRedirectWebFilter redirect404ToRoot() {
		return new ResourceNotFoundRedirectWebFilter();
	}
}