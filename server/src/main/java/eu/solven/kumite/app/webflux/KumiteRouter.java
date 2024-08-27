package eu.solven.kumite.app.webflux;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import eu.solven.kumite.contest.ContestSearchHandler;
import eu.solven.kumite.game.GameSearchHandler;
import eu.solven.kumite.greeting.GreetingHandler;
import eu.solven.kumite.leaderboard.LeaderboardHandler;
import eu.solven.kumite.webhook.WebhooksHandler;

@Configuration(proxyBeanMethods = false)
public class KumiteRouter {

	@Bean
	public RouterFunction<ServerResponse> route(GreetingHandler greetingHandler,
			GameSearchHandler gamesSearchHandler,
			ContestSearchHandler contestSearchHandler,
			LeaderboardHandler leaderboardHandler,
			WebhooksHandler webhooksHandler) {
		return RouterFunctions
				.route(RequestPredicates.GET("/hello").and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
						greetingHandler::hello)
				.and(RouterFunctions.route(
						RequestPredicates.GET("/game").and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
						gamesSearchHandler::listGames))
				.and(RouterFunctions.route(
						RequestPredicates.GET("/contest").and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
						contestSearchHandler::listContests))
				.and(RouterFunctions.route(
						RequestPredicates.PUT("/contest").and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
						contestSearchHandler::generateContest))
				.and(RouterFunctions.route(
						RequestPredicates.GET("/leaderboard").and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
						leaderboardHandler::listScores))

				.and(RouterFunctions.route(
						RequestPredicates.GET("/webhook").and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
						webhooksHandler::listWebhooks))
				.and(RouterFunctions.route(
						RequestPredicates.PUT("/webhook").and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
						webhooksHandler::registerWebhook))
				.and(RouterFunctions.route(
						RequestPredicates.DELETE("/webhook").and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
						webhooksHandler::dropWebhooks));
	}
}