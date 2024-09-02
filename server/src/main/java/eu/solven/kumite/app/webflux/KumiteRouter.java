package eu.solven.kumite.app.webflux;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import eu.solven.kumite.app.greeting.GreetingHandler;
import eu.solven.kumite.board.BoardHandler;
import eu.solven.kumite.contest.ContestSearchHandler;
import eu.solven.kumite.game.GameSearchHandler;
import eu.solven.kumite.leaderboard.LeaderboardHandler;
import eu.solven.kumite.player.PlayerMovesHandler;
import eu.solven.kumite.player.PlayersSearchHandler;
import eu.solven.kumite.webhook.WebhooksHandler;

/**
 * Redirect each route (e.g. `/games/someGameId`) to the appropriate handler.
 * 
 * @author Benoit Lacelle
 *
 */
@Configuration(proxyBeanMethods = false)
public class KumiteRouter {

	@Value("classpath:/static/index.html")
	private Resource indexHtml;

	@Bean
	public RouterFunction<ServerResponse> route(GreetingHandler greetingHandler,
			GameSearchHandler gamesSearchHandler,
			PlayersSearchHandler playersSearchHandler,
			ContestSearchHandler contestSearchHandler,
			BoardHandler boardHandler,
			PlayerMovesHandler playerMovesHandler,
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

				.and(RouterFunctions.route(RequestPredicates.GET("/api/board").and(json), boardHandler::getBoard))
				.and(RouterFunctions.route(RequestPredicates.GET("/api/board/moves").and(json),
						playerMovesHandler::listPlayerMoves))
				.and(RouterFunctions.route(RequestPredicates.POST("/api/board/move").and(json),
						playerMovesHandler::registerPlayerMove))

				.and(RouterFunctions.route(RequestPredicates.GET("/api/leaderboards").and(json),
						leaderboardHandler::listScores))

				.and(RouterFunctions.route(RequestPredicates.GET("/api/webhooks").and(json),
						webhooksHandler::listWebhooks))
				.and(RouterFunctions.route(RequestPredicates.PUT("/api/webhooks").and(json),
						webhooksHandler::registerWebhook))
				.and(RouterFunctions.route(RequestPredicates.DELETE("/api/webhooks").and(json),
						webhooksHandler::dropWebhooks))

				// The following routes are useful for the SinglePageApplication
				.and(RouterFunctions.route(
						RequestPredicates.GET("/games/**").and(RequestPredicates.accept(MediaType.TEXT_HTML)),
						request -> ServerResponse.ok().contentType(MediaType.TEXT_HTML).bodyValue(indexHtml)))
				.and(RouterFunctions.route(
						RequestPredicates.GET("/contests/**").and(RequestPredicates.accept(MediaType.TEXT_HTML)),
						request -> ServerResponse.ok().contentType(MediaType.TEXT_HTML).bodyValue(indexHtml)))
				.and(RouterFunctions.route(
						RequestPredicates.GET("/about/**").and(RequestPredicates.accept(MediaType.TEXT_HTML)),
						request -> ServerResponse.ok().contentType(MediaType.TEXT_HTML).bodyValue(indexHtml)));
	}

	@Bean
	public ResourceNotFoundRedirectWebFilter redirect404ToRoot() {
		return new ResourceNotFoundRedirectWebFilter();
	}
}