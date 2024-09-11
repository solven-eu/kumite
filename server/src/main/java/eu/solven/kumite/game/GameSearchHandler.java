package eu.solven.kumite.game;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import eu.solven.kumite.app.controllers.KumiteHandlerHelper;
import eu.solven.kumite.game.GameSearchParameters.GameSearchParametersBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
public class GameSearchHandler {
	final GamesRegistry gamesRegistry;

	public Mono<ServerResponse> listGames(ServerRequest request) {
		GameSearchParametersBuilder parameters = GameSearchParameters.builder();

		KumiteHandlerHelper.optUuid(request, "game_id").ifPresent(id -> parameters.gameId(Optional.of(id)));

		Optional<String> optMinPlayers = request.queryParam("min_players");
		optMinPlayers.ifPresent(rawMin -> parameters.minPlayers(OptionalInt.of(Integer.parseInt(rawMin))));

		Optional<String> optMaxPlayers = request.queryParam("max_players");
		optMaxPlayers.ifPresent(rawMax -> parameters.maxPlayers(OptionalInt.of(Integer.parseInt(rawMax))));

		Optional<String> optTitle = request.queryParam("title_regex");
		optTitle.ifPresent(rawTitle -> parameters.titleRegex(Optional.of(rawTitle)));

		List<GameMetadata> games = gamesRegistry.searchGames(parameters.build());
		log.info("Games for {}: {}", parameters, games);
		return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(games));
	}
}