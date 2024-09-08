package eu.solven.kumite.contest;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.solven.kumite.board.BoardsRegistry;
import eu.solven.kumite.board.IHasBoard;
import eu.solven.kumite.board.IKumiteBoard;
import eu.solven.kumite.contest.ContestMetadata.ContestMetadataBuilder;
import eu.solven.kumite.contest.ContestSearchParameters.ContestSearchParametersBuilder;
import eu.solven.kumite.game.GamesRegistry;
import eu.solven.kumite.game.IGame;
import eu.solven.kumite.player.ContestPlayersRegistry;
import eu.solven.kumite.player.IHasPlayers;
import eu.solven.kumite.tools.IUuidGenerator;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Builder
public class ContestSearchHandler {

	@NonNull
	final GamesRegistry gamesRegistry;

	@NonNull
	final ContestsRegistry contestsRegistry;

	@NonNull
	final ContestPlayersRegistry contestPlayersRegistry;

	@NonNull
	final BoardsRegistry boardsRegistry;

	@NonNull
	final IUuidGenerator uuidGenerator;

	public Mono<ServerResponse> listContests(ServerRequest request) {
		ContestSearchParametersBuilder parameters = ContestSearchParameters.builder();

		Optional<UUID> optUuid = KumiteHandlerHelper.optUuid(request, "contest_id");
		optUuid.ifPresent(uuid -> parameters.contestId(Optional.of(uuid)));

		Optional<UUID> optGame = KumiteHandlerHelper.optUuid(request, "game_id");
		optGame.ifPresent(rawGameUuid -> parameters.gameId(Optional.of(rawGameUuid)));

		Optional<String> optMorePlayers = request.queryParam("accept_players");
		optMorePlayers.ifPresent(rawMorePlayers -> parameters.acceptPlayers(Boolean.parseBoolean(rawMorePlayers)));

		Optional<String> optRequirePlayers = request.queryParam("requires_players");
		optRequirePlayers.ifPresent(rawBeingPlayed -> parameters.requirePlayers(Boolean.parseBoolean(rawBeingPlayed)));

		Optional<String> optGameOver = request.queryParam("game_over");
		optGameOver.ifPresent(rawGameOver -> parameters.gameOver(Boolean.parseBoolean(rawGameOver)));

		return ServerResponse.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromValue(contestsRegistry.searchContests(parameters.build())
						.stream()
						.map(ContestMetadataRaw::snapshot)
						.collect(Collectors.toList())));
	}

	public Mono<ServerResponse> generateContest(ServerRequest request) {
		UUID gameId = KumiteHandlerHelper.uuid(request, "game_id");

		IGame game = gamesRegistry.getGame(gameId);

		UUID contestId = uuidGenerator.randomUUID();

		ContestMetadataBuilder parameters = ContestMetadata.builder().contestId(contestId);

		parameters.gameMetadata(game.getGameMetadata());

		return request.bodyToMono(Map.class).<ServerResponse>flatMap(contestBody -> {
			ObjectMapper objectMapper = new ObjectMapper();
			Map<String, ?> rawConstantMetdata = (Map<String, ?>) contestBody.get("constant_metadata");

			// TODO Check input values, especially minPlayers and maxPlayers given IGame own constrains
			parameters.constantMetadata(objectMapper.convertValue(rawConstantMetdata, ContestCreationMetadata.class));

			Map<String, ?> rawBoard = (Map<String, ?>) contestBody.get("board");
			IKumiteBoard board = game.parseRawBoard(rawBoard);

			boardsRegistry.registerBoard(contestId, board);

			IHasBoard hasBoard = boardsRegistry.makeDynamicBoardHolder(contestId);
			IHasPlayers hasPlayers = contestPlayersRegistry.makeDynamicHasPlayers(contestId);

			Contest contest = Contest.builder()
					.contestMetadata(parameters.build())
					.game(game)
					.board(hasBoard)
					.hasPlayers(hasPlayers)
					.build();

			return ServerResponse.ok()
					.contentType(MediaType.APPLICATION_JSON)
					.body(BodyInserters.fromValue(contestsRegistry.registerContest(contest)));
		});
	}

}