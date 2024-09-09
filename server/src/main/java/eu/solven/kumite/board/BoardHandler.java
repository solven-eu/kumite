package eu.solven.kumite.board;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import eu.solven.kumite.contest.ContestMetadata;
import eu.solven.kumite.contest.ContestSearchParameters;
import eu.solven.kumite.contest.ContestSearchParameters.ContestSearchParametersBuilder;
import eu.solven.kumite.contest.ContestsRegistry;
import eu.solven.kumite.contest.KumiteHandlerHelper;
import eu.solven.kumite.game.GamesRegistry;
import eu.solven.kumite.game.IGame;
import eu.solven.kumite.game.optimization.tsp.IKumiteBoardView;
import eu.solven.kumite.player.ContestPlayersRegistry;
import eu.solven.kumite.player.KumitePlayer;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import reactor.core.publisher.Mono;

@AllArgsConstructor
public class BoardHandler {
	@NonNull
	final GamesRegistry gamesRegistry;

	@NonNull
	final ContestsRegistry contestsRegistry;

	@NonNull
	final ContestPlayersRegistry contestPlayersRegistry;

	@NonNull
	final BoardsRegistry boardsRegistry;

	public Mono<ServerResponse> getBoard(ServerRequest request) {
		UUID contestId = KumiteHandlerHelper.uuid(request, "contest_id");
		UUID playerId = KumiteHandlerHelper.uuid(request, "player_id");

		boolean playerHasJoined = contestPlayersRegistry.isRegisteredPlayer(contestId, playerId);

		boolean accountIsViewing;
		boolean playerCanJoin;
		if (playerHasJoined) {
			// A player can not join twice a contest
			playerCanJoin = false;
			// A player can not both play and view
			accountIsViewing = false;
		} else {
			accountIsViewing = contestPlayersRegistry.isViewing(contestId, playerId);

			if (accountIsViewing) {
				// A player can not join if it is viewing
				playerCanJoin = false;
			} else {
				ContestSearchParametersBuilder parameters = ContestSearchParameters.builder();
				List<ContestMetadata> contest =
						contestsRegistry.searchContests(parameters.contestId(Optional.of(contestId)).build());
				if (contest.isEmpty()) {
					throw new IllegalArgumentException("No contest for contestId=" + contestId);
				} else if (contest.size() >= 2) {
					throw new IllegalStateException(
							"Multiple contests for contestId=" + contestId + " contests=" + contest);
				}

				ContestMetadata contestMetadata = contest.get(0);

				IGame game = gamesRegistry.getGame(contestMetadata.getGameMetadata().getGameId());

				playerCanJoin =
						game.canAcceptPlayer(contestMetadata, KumitePlayer.builder().playerId(playerId).build());
			}
		}

		IKumiteBoard board = boardsRegistry.makeDynamicBoardHolder(contestId).get();

		UUID viewPlayerId;
		if (playerHasJoined) {
			viewPlayerId = playerId;
		} else if (accountIsViewing) {
			viewPlayerId = KumitePlayer.AUDIENCE_PLAYER_ID;
		} else {
			viewPlayerId = KumitePlayer.PREVIEW_PLAYER_ID;
		}

		// TODO Check if the playerId is the public one, or a viewer, or a player
		IKumiteBoardView boardView = board.asView(viewPlayerId);

		Map<String, Object> output = Map.of("contestId",
				contestId,
				"playerId",
				playerId,
				"playerHasJoined",
				playerHasJoined,
				"playerCanJoin",
				playerCanJoin,
				"accountIsViewing",
				accountIsViewing,
				"board",
				boardView);

		return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(output));
	}
}