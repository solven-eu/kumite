package eu.solven.kumite.player;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.solven.kumite.app.controllers.KumiteHandlerHelper;
import eu.solven.kumite.board.IKumiteBoardView;
import eu.solven.kumite.contest.Contest;
import eu.solven.kumite.contest.ContestView;
import eu.solven.kumite.contest.ContestsRegistry;
import eu.solven.kumite.game.GamesRegistry;
import eu.solven.kumite.game.IGame;
import eu.solven.kumite.lifecycle.BoardLifecycleManager;
import eu.solven.kumite.player.PlayerJoinRaw.PlayerJoinRawBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
public class PlayerMovesHandler {
	final GamesRegistry gamesRegistry;
	final ContestsRegistry contestsRegistry;

	final BoardLifecycleManager boardLifecycleManager;

	public Mono<ServerResponse> registerPlayer(ServerRequest request) {
		PlayerJoinRawBuilder parameters = PlayerJoinRaw.builder();

		UUID playerId = KumiteHandlerHelper.uuid(request, "player_id");
		parameters.playerId(playerId);

		UUID contestId = KumiteHandlerHelper.uuid(request, "contest_id");

		Optional<Boolean> optViewer = KumiteHandlerHelper.optBoolean(request, "viewer");
		optViewer.ifPresent(viewer -> parameters.isViewer(viewer));

		Contest contest = contestsRegistry.getContest(contestId);

		PlayerJoinRaw playerJoinRaw = parameters.build();
		boardLifecycleManager.registerPlayer(contest, playerJoinRaw);

		boolean isViewer = playerJoinRaw.isViewer();
		PlayingPlayer playingPlayer = PlayingPlayer.builder()
				.playerId(playerId)

				.playerHasJoined(!isViewer)
				.playerCanJoin(false)
				.accountIsViewing(isViewer)
				.build();

		return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(playingPlayer));
	}

	// This would return a list of possible moves. The list may not be exhaustive, but indicative.
	// We may introduce a separate API to output moves with metadata (e.g. with the range of availables values for given
	// property of a move)
	public Mono<ServerResponse> listPlayerMoves(ServerRequest request) {
		UUID playerId = KumiteHandlerHelper.uuid(request, "player_id");
		UUID contestId = KumiteHandlerHelper.uuid(request, "contest_id");

		Contest contest = contestsRegistry.getContest(contestId);
		IGame game = gamesRegistry.getGame(contest.getGameMetadata().getGameId());

		IKumiteBoardView boardView = contest.getBoard().get().asView(playerId);
		Map<String, IKumiteMove> moves = game.exampleMoves(boardView, playerId);

		PlayerMovesHolder movesHolder = PlayerMovesHolder.builder().moves(moves).build();

		return ServerResponse.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromValue(PlayerMovesHolder.snapshot(movesHolder)));
	}

	public Mono<ServerResponse> playMove(ServerRequest request) {
		UUID playerId = KumiteHandlerHelper.uuid(request, "player_id");
		UUID contestId = KumiteHandlerHelper.uuid(request, "contest_id");

		Contest contest = contestsRegistry.getContest(contestId);
		IGame game = gamesRegistry.getGame(contest.getGameMetadata().getGameId());

		return request.bodyToMono(Map.class).<ServerResponse>flatMap(rawMove -> {
			IKumiteMove move = game.parseRawMove(rawMove);

			PlayerMoveRaw playerMove = PlayerMoveRaw.builder().playerId(playerId).move(move).build();
			IKumiteBoardView boardViewPostMove = boardLifecycleManager.onPlayerMove(contest, playerMove);

			ObjectMapper objectMapper = new ObjectMapper();

			ContestView view = ContestView.builder()
					.contestId(contestId)
					.playingPlayer(PlayingPlayer.player(playerId))
					.board(objectMapper.convertValue(boardViewPostMove, Map.class))
					.dynamicMetadata(Contest.snapshot(contest).getDynamicMetadata())
					.build();

			return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(view));
		});
	}
}