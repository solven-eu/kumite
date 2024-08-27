package eu.solven.kumite.contest;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import eu.solven.kumite.board.BoardAndPlayers;
import eu.solven.kumite.board.IKumiteBoard;
import eu.solven.kumite.contest.ContestMetadata.ContestMetadataBuilder;
import eu.solven.kumite.contest.ContestSearchParameters.ContestSearchParametersBuilder;
import eu.solven.kumite.game.GamesStore;
import eu.solven.kumite.game.IGame;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import reactor.core.publisher.Mono;

@Value
@Builder
public class ContestSearchHandler {
	@NonNull
	ContestsStore contestsStore;

	@NonNull
	GamesStore gamesStore;

	public Mono<ServerResponse> listContests(ServerRequest request) {
		ContestSearchParametersBuilder parameters = ContestSearchParameters.builder();

		Optional<String> optUuid = request.queryParam("contest_id");
		optUuid.ifPresent(rawUuid -> parameters.contestId(Optional.of(UUID.fromString(rawUuid))));

		Optional<String> optGame = request.queryParam("game_id");
		optGame.ifPresent(rawGameUuid -> parameters.gameId(Optional.of(UUID.fromString(rawGameUuid))));

		Optional<String> optMorePlayers = request.queryParam("accept_players");
		optMorePlayers.ifPresent(rawMorePlayers -> parameters.acceptPlayers(Boolean.parseBoolean(rawMorePlayers)));

		Optional<String> optRequirePlayers = request.queryParam("requires_players");
		optRequirePlayers.ifPresent(rawBeingPlayed -> parameters.requirePlayers(Boolean.parseBoolean(rawBeingPlayed)));

		Optional<String> optGameOver = request.queryParam("game_over");
		optGameOver.ifPresent(rawGameOver -> parameters.gameOver(Boolean.parseBoolean(rawGameOver)));

		return ServerResponse.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromValue(contestsStore.searchContests(parameters.build())
						.stream()
						.map(ContestMetadataRaw::snapshot)
						.collect(Collectors.toList())));
	}

	public Mono<ServerResponse> generateContest(ServerRequest request) {
		UUID gameId = UUID.fromString(request.queryParam("game_id").get());

		IGame game = gamesStore.getGame(gameId);

		ContestMetadataBuilder parameters = ContestMetadata.builder();

		parameters.gameMetadata(game.getGameMetadata());

		return request.bodyToMono(Map.class).<ServerResponse>flatMap(rawBoard -> {
			IKumiteBoard board = game.parseRawBoard(rawBoard);

			Contest contest = Contest.builder()
					.contestMetadata(parameters.build())
					.refBoard(BoardAndPlayers.builder().game(game).board(board).hasPlayers(null).build())
					.build();

			return ServerResponse.ok()
					.contentType(MediaType.APPLICATION_JSON)
					.body(BodyInserters.fromValue(contestsStore.registerContest(contest)));
		});
	}

}