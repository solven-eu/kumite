package eu.solven.kumite.app.webflux;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicate;
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
		RequestPredicate json = RequestPredicates.accept(MediaType.APPLICATION_JSON);

		return RouterFunctions.route(RequestPredicates.GET("/hello").and(json), greetingHandler::hello)
				.and(RouterFunctions.route(RequestPredicates.GET("/games").and(json), gamesSearchHandler::listGames))

				.and(RouterFunctions.route(RequestPredicates.GET("/contests").and(json),
						contestSearchHandler::listContests))
				.and(RouterFunctions.route(RequestPredicates.PUT("/contests").and(json),
						contestSearchHandler::generateContest))

				.and(RouterFunctions.route(RequestPredicates.GET("/leaderboards").and(json),
						leaderboardHandler::listScores))

				.and(RouterFunctions.route(RequestPredicates.GET("/webhooks").and(json), webhooksHandler::listWebhooks))
				.and(RouterFunctions.route(RequestPredicates.PUT("/webhooks").and(json),
						webhooksHandler::registerWebhook))
				.and(RouterFunctions.route(RequestPredicates.DELETE("/webhooks").and(json),
						webhooksHandler::dropWebhooks));
	}
}