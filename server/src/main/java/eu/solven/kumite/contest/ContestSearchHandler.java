package eu.solven.kumite.contest;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import eu.solven.kumite.board.BoardsRegistry;
import eu.solven.kumite.board.IHasBoard;
import eu.solven.kumite.board.IKumiteBoard;
import eu.solven.kumite.contest.ContestMetadata.ContestMetadataBuilder;
import eu.solven.kumite.contest.ContestSearchParameters.ContestSearchParametersBuilder;
import eu.solven.kumite.game.GamesRegistry;
import eu.solven.kumite.game.IGame;
import eu.solven.kumite.player.ContestPlayersRegistry;
import eu.solven.kumite.player.IHasPlayers;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Builder
public class ContestSearchHandler {
	@NonNull
	final ContestsRegistry contestsStore;

	@NonNull
	final GamesRegistry gamesStore;

	@NonNull
	final ContestPlayersRegistry contestPlayerRegistry;

	@NonNull
	final BoardsRegistry boardRegistry;

	public Mono<ServerResponse> listContests(ServerRequest request) {
		ContestSearchParametersBuilder parameters = ContestSearchParameters.builder();

		Optional<String> optUuid = request.queryParam("contest_id");
		optUuid.ifPresent(rawUuid -> parameters.contestId(Optional.of(KumiteHandlerHelper.uuid(rawUuid))));

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

		UUID contestId = UUID.randomUUID();

		ContestMetadataBuilder parameters = ContestMetadata.builder().contestId(contestId);

		parameters.gameMetadata(game.getGameMetadata());

		return request.bodyToMono(Map.class).<ServerResponse>flatMap(rawBoard -> {
			IKumiteBoard board = game.parseRawBoard(rawBoard);

			boardRegistry.registerBoard(contestId, board);

			IHasBoard hasBoard = boardRegistry.makeDynamicBoardHolder(contestId);
			IHasPlayers hasPlayers = contestPlayerRegistry.makeDynamicHasPlayers(contestId);

			Contest contest = Contest.builder()
					.contestMetadata(parameters.build())
					.game(game)
					.board(hasBoard)
					.hasPlayers(hasPlayers)
					// .refBoard(BoardAndPlayers.builder().game(game).board(hasBoard).hasPlayers(hasPlayers).build())
					.build();

			return ServerResponse.ok()
					.contentType(MediaType.APPLICATION_JSON)
					.body(BodyInserters.fromValue(contestsStore.registerContest(contest)));
		});
	}

}