package eu.solven.kumite.player;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.random.RandomGenerator;

import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import eu.solven.kumite.app.webflux.api.KumiteHandlerHelper;
import eu.solven.kumite.board.BoardLifecycleManager;
import eu.solven.kumite.board.IKumiteBoardView;
import eu.solven.kumite.contest.Contest;
import eu.solven.kumite.contest.ContestView;
import eu.solven.kumite.contest.ContestsRegistry;
import eu.solven.kumite.game.GamesRegistry;
import eu.solven.kumite.game.IGame;
import eu.solven.kumite.move.IKumiteMove;
import eu.solven.kumite.move.PlayerMoveRaw;
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
	final RandomGenerator randomGenerator;

	public Mono<ServerResponse> registerPlayer(ServerRequest request) {
		PlayerJoinRawBuilder parameters = PlayerJoinRaw.builder();

		UUID playerId = KumiteHandlerHelper.uuid(request, "player_id");
		parameters.playerId(playerId);

		UUID contestId = KumiteHandlerHelper.uuid(request, "contest_id");

		Optional<Boolean> optViewer = KumiteHandlerHelper.optBoolean(request, "viewer");
		optViewer.ifPresent(viewer -> parameters.isViewer(viewer));

		Contest contest = contestsRegistry.getContest(contestId);

		PlayerJoinRaw playerJoinRaw = parameters.build();
		IKumiteBoardView view = boardLifecycleManager.registerPlayer(contest, playerJoinRaw);
		log.debug("Should we return the sync view={} on playerRegistration?", view);

		boolean isViewer = playerJoinRaw.isViewer();
		PlayerContestStatus playingPlayer = PlayerContestStatus.builder()
				.playerId(playerId)

				.playerHasJoined(!isViewer)
				.playerCanJoin(false)
				.accountIsViewing(isViewer)
				.build();

		return KumiteHandlerHelper.okAsJson(playingPlayer);
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
		Map<String, IKumiteMove> moves = game.exampleMoves(randomGenerator, boardView, playerId);

		PlayerMovesHolder movesHolder = PlayerMovesHolder.builder().moves(moves).build();

		return KumiteHandlerHelper.okAsJson(PlayerMovesHolder.snapshot(movesHolder));
	}

	public Mono<ServerResponse> playMove(ServerRequest request) {
		UUID playerId = KumiteHandlerHelper.uuid(request, "player_id");
		UUID contestId = KumiteHandlerHelper.uuid(request, "contest_id");

		Contest contestPreMove = contestsRegistry.getContest(contestId);
		IGame game = gamesRegistry.getGame(contestPreMove.getGameMetadata().getGameId());

		return request.bodyToMono(Map.class).map(rawMove -> {
			IKumiteMove move = game.parseRawMove(rawMove);

			PlayerMoveRaw playerMove = PlayerMoveRaw.builder().playerId(playerId).move(move).build();
			IKumiteBoardView boardViewPostMove = boardLifecycleManager.onPlayerMove(contestPreMove, playerMove);

			ContestView view = ContestView.fromView(boardViewPostMove)
					.contestId(contestId)
					.playerStatus(PlayerContestStatus.contender(playerId))
					.dynamicMetadata(Contest.snapshot(contestPreMove).getDynamicMetadata())
					.build();

			return view;
		}).flatMap(KumiteHandlerHelper::okAsJson);
	}
}