package eu.solven.kumite.app.webflux;

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
public class KumiteApiRouter {

	// https://github.com/springdoc/springdoc-openapi-demos/tree/2.x/springdoc-openapi-spring-boot-2-webflux-functional
	// https://stackoverflow.com/questions/6845772/should-i-use-singular-or-plural-name-convention-for-rest-resources
	@Bean
	public RouterFunction<ServerResponse> apiRoutes(GreetingHandler greetingHandler,
			GameSearchHandler gamesSearchHandler,
			PlayersSearchHandler playersSearchHandler,
			ContestSearchHandler contestSearchHandler,
			BoardHandler boardHandler,
			PlayerMovesHandler playerMovesHandler,
			LeaderboardHandler leaderboardHandler,
			WebhooksHandler webhooksHandler) {
		RequestPredicate json = RequestPredicates.accept(MediaType.APPLICATION_JSON);

		Builder gameId = parameterBuilder().name("game_id").description("Search for a specific contestId");
		Builder playerId = parameterBuilder().name("player_id").description("Search for a specific playerId");
		Builder contestId = parameterBuilder().name("contest_id").description("Search for a specific contestId");

		return SpringdocRouteBuilder.route()
				.GET(RequestPredicates.GET("/api/hello").and(json),
						greetingHandler::hello,
						ops -> ops.operationId("hello"))

				.GET(RequestPredicates.GET("/api/games").and(json),
						gamesSearchHandler::listGames,
						ops -> ops.operationId("searchGames")
								.parameter(gameId)
								.parameter(parameterBuilder().name("min_players")
										.description("Search games with at most given minPlayers"))
								.parameter(parameterBuilder().name("max_players")
										.description("Search games with at least given maxPlayers"))
								.parameter(parameterBuilder().name("title_regex")
										.description("Search games with a title matching given Regex"))
								.response(responseBuilder().responseCode("200").description("Hello")))

				.GET(RequestPredicates.GET("/api/players").and(json),
						playersSearchHandler::listPlayers,
						ops -> ops.operationId("searchPlayers"))

				.GET(RequestPredicates.GET("/api/contests").and(json),
						contestSearchHandler::listContests,
						ops -> ops.operationId("searchContest").parameter(gameId))
				.POST(RequestPredicates.POST("/api/contests").and(json),
						contestSearchHandler::generateContest,
						ops -> ops.operationId("publishContest"))

				.GET(RequestPredicates.GET("/api/board").and(json),
						boardHandler::getBoard,
						ops -> ops.operationId("fetchBoard").parameter(playerId).parameter(contestId))
				.POST(RequestPredicates.POST("/api/board/player").and(json),
						playerMovesHandler::registerPlayer,
						ops -> ops.operationId("registerPlayer")
								.parameter(playerId)
								.parameter(contestId)
								.parameter(parameterBuilder().name("viewer")
										.description("`true` if you want to spectate the contest")
										.implementation(Boolean.class)))
				.GET(RequestPredicates.GET("/api/board/moves").and(json),
						playerMovesHandler::listPlayerMoves,
						ops -> ops.operationId("fetchMoves").parameter(playerId).parameter(contestId))
				.POST(RequestPredicates.POST("/api/board/move").and(json),
						playerMovesHandler::playMove,
						ops -> ops.operationId("playMove").parameter(playerId).parameter(contestId))

				.GET(RequestPredicates.GET("/api/leaderboards").and(json),
						leaderboardHandler::listScores,
						ops -> ops.operationId("fetchLeaderboard").parameter(contestId))

				// Activate webhooks later. For now, we focus on long-polling
				// .GET(RequestPredicates.GET("/api/webhooks").and(json),
				// webhooksHandler::listWebhooks,
				// ops -> ops.operationId("listWebhooks"))
				// .PUT(RequestPredicates.PUT("/api/webhooks").and(json),
				// webhooksHandler::registerWebhook,
				// ops -> ops.operationId("publishWebhook"))
				// .DELETE(RequestPredicates.DELETE("/api/webhooks").and(json),
				// webhooksHandler::dropWebhooks,
				// ops -> ops.operationId("deleteWebhook"))

				.build();
	}
}