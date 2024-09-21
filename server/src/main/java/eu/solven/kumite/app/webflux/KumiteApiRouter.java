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

import eu.solven.kumite.board.BoardHandler;
import eu.solven.kumite.contest.ContestSearchHandler;
import eu.solven.kumite.game.GameSearchHandler;
import eu.solven.kumite.leaderboard.LeaderboardHandler;
import eu.solven.kumite.player.PlayerMovesHandler;
import eu.solven.kumite.player.PlayersSearchHandler;
import eu.solven.kumite.webhook.WebhooksHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * Redirect each route (e.g. `/games/someGameId`) to the appropriate handler.
 * 
 * @author Benoit Lacelle
 *
 */
@Configuration(proxyBeanMethods = false)
@Slf4j
public class KumiteApiRouter {

	private static final RequestPredicate json(String path) {
		final RequestPredicate json = RequestPredicates.accept(MediaType.APPLICATION_JSON);
		return RequestPredicates.path("/api/v1" + path).and(json);
	}

	@Bean
	PlayerVerifierFilterFunction playerVerifierFilterFunction() {
		return new PlayerVerifierFilterFunction();
	}

	// https://github.com/springdoc/springdoc-openapi-demos/tree/2.x/springdoc-openapi-spring-boot-2-webflux-functional
	// https://stackoverflow.com/questions/6845772/should-i-use-singular-or-plural-name-convention-for-rest-resources
	@Bean
	public RouterFunction<ServerResponse> apiRoutes(PlayerVerifierFilterFunction playerVerifierFilterFunction,
			GameSearchHandler gamesSearchHandler,
			PlayersSearchHandler playersSearchHandler,
			ContestSearchHandler contestSearchHandler,
			BoardHandler boardHandler,
			PlayerMovesHandler playerMovesHandler,
			LeaderboardHandler leaderboardHandler,
			WebhooksHandler webhooksHandler) {

		Builder gameId = parameterBuilder().name("game_id").description("Search for a specific contestId");
		Builder playerId = parameterBuilder().name("player_id").description("Search for a specific playerId");
		Builder contestId = parameterBuilder().name("contest_id").description("Search for a specific contestId");

		return SpringdocRouteBuilder.route()
				.GET(json("/games"),
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

				.GET(json("/players"), playersSearchHandler::listPlayers, ops -> ops.operationId("searchPlayers"))

				.GET(json("/contests"),
						contestSearchHandler::listContests,
						ops -> ops.operationId("searchContest").parameter(gameId))
				.POST(json("/contests"),
						contestSearchHandler::generateContest,
						ops -> ops.operationId("publishContest"))

				.GET(json("/board"),
						boardHandler::getBoard,
						ops -> ops.operationId("fetchBoard").parameter(playerId).parameter(contestId))
				.POST(json("/board/player"),
						playerMovesHandler::registerPlayer,
						ops -> ops.operationId("registerPlayer")
								.parameter(playerId)
								.parameter(contestId)
								.parameter(parameterBuilder().name("viewer")
										.description("`true` if you want to spectate the contest")
										.implementation(Boolean.class)))
				.GET(json("/board/moves"),
						playerMovesHandler::listPlayerMoves,
						ops -> ops.operationId("fetchMoves").parameter(playerId).parameter(contestId))
				.POST(json("/board/move"),
						playerMovesHandler::playMove,
						ops -> ops.operationId("playMove").parameter(playerId).parameter(contestId))

				.GET(json("/leaderboards"),
						leaderboardHandler::listScores,
						ops -> ops.operationId("fetchLeaderboard").parameter(contestId))

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

				.filter(playerVerifierFilterFunction, ops -> {
				})
				.build();

	}
}