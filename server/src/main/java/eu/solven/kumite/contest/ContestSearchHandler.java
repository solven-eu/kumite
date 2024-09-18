package eu.solven.kumite.contest;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.solven.kumite.app.KumiteJackson;
import eu.solven.kumite.app.controllers.KumiteHandlerHelper;
import eu.solven.kumite.board.IKumiteBoard;
import eu.solven.kumite.contest.ContestSearchParameters.ContestSearchParametersBuilder;
import eu.solven.kumite.game.GameMetadata;
import eu.solven.kumite.game.GamesRegistry;
import eu.solven.kumite.game.IGame;
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
	final RandomGenerator randomGenerator;

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
						.map(Contest::snapshot)
						.collect(Collectors.toList())));
	}

	// BEWARE we coupled the generation of a contest and its board. This may be poor design.
	public Mono<ServerResponse> generateContest(ServerRequest request) {
		UUID gameId = KumiteHandlerHelper.uuid(request, "game_id");

		// TODO Get this from JWT
		UUID authorAccountId = UUID.randomUUID();

		IGame game = gamesRegistry.getGame(gameId);

		return request.bodyToMono(Map.class).<ServerResponse>flatMap(contestBody -> {
			Map<String, ?> rawConstantMetadata = (Map<String, ?>) contestBody.get("constant_metadata");

			ContestCreationMetadata constantMetadata =
					validateConstantMetadata(authorAccountId, rawConstantMetadata, game.getGameMetadata());

			Map<String, ?> rawBoard = (Map<String, ?>) contestBody.get("board");

			IKumiteBoard board;
			if (rawBoard == null) {
				board = game.generateInitialBoard(randomGenerator);
			} else {
				board = game.parseRawBoard(rawBoard);
			}

			Contest registeredContest = contestsRegistry.registerContest(game, constantMetadata, board);
			ContestMetadataRaw snapshot = Contest.snapshot(registeredContest);

			return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(snapshot));
		});
	}

	private ContestCreationMetadata validateConstantMetadata(UUID authorAccountId,
			Map<String, ?> rawConstantMetadata,
			GameMetadata gameMetadata) {
		if (rawConstantMetadata.containsKey("created")) {
			throw new IllegalArgumentException("`created` must not be provided by the author");
		}

		// Name is the only required parameter
		String contestName = rawConstantMetadata.get("name").toString();

		// Prepare a contestMetdata with default parameters
		ContestCreationMetadata defaultContestMetadata =
				ContestCreationMetadata.fromGame(gameMetadata).name(contestName).author(authorAccountId).build();

		ObjectMapper objectMapper = KumiteJackson.objectMapper();
		// Convert the default metadata into Map
		Map<String, Object> rawMergedContestMetadata = objectMapper.convertValue(defaultContestMetadata, Map.class);
		// Merge the custom metadata over the default metadata
		rawMergedContestMetadata.putAll(rawConstantMetadata);

		ContestCreationMetadata mergedContestMetadata =
				objectMapper.convertValue(rawMergedContestMetadata, ContestCreationMetadata.class);

		if (mergedContestMetadata.getMinPlayers() < gameMetadata.getMinPlayers()) {
			throw new IllegalArgumentException("Invalid minPlayers: " + mergedContestMetadata.getMinPlayers());
		} else if (mergedContestMetadata.getMaxPlayers() > gameMetadata.getMaxPlayers()) {
			throw new IllegalArgumentException("Invalid maxPlayers: " + mergedContestMetadata.getMaxPlayers());
		}

		return mergedContestMetadata;
	}

}