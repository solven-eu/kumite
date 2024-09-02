package eu.solven.kumite.game;

import java.util.Optional;
import java.util.OptionalInt;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import eu.solven.kumite.contest.KumiteHandlerHelper;
import eu.solven.kumite.game.GameSearchParameters.GameSearchParametersBuilder;
import lombok.Value;
import reactor.core.publisher.Mono;

@Value
public class GameSearchHandler {
	GamesRegistry gamesStore;

	public Mono<ServerResponse> listGames(ServerRequest request) {
		GameSearchParametersBuilder parameters = GameSearchParameters.builder();

		KumiteHandlerHelper.optUuid(request, "game_id").ifPresent(id -> parameters.gameId(Optional.of(id)));

		Optional<String> optMinPlayers = request.queryParam("min_players");
		optMinPlayers.ifPresent(rawMin -> parameters.minPlayers(OptionalInt.of(Integer.parseInt(rawMin))));

		Optional<String> optMaxPlayers = request.queryParam("max_players");
		optMaxPlayers.ifPresent(rawMax -> parameters.maxPlayers(OptionalInt.of(Integer.parseInt(rawMax))));

		Optional<String> optTitle = request.queryParam("title");
		optTitle.ifPresent(rawTitle -> parameters.titlePattern(Optional.of(rawTitle)));

		return ServerResponse.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromValue(gamesStore.searchGames(parameters.build())));
	}
}