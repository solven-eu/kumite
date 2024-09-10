package eu.solven.kumite.player;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import eu.solven.kumite.app.controllers.KumiteHandlerHelper;
import eu.solven.kumite.contest.Contest;
import eu.solven.kumite.contest.ContestsRegistry;
import eu.solven.kumite.game.GamesRegistry;
import eu.solven.kumite.game.IGame;
import eu.solven.kumite.game.optimization.tsp.IKumiteBoardView;
import eu.solven.kumite.lifecycle.BoardLifecycleManager;
import eu.solven.kumite.player.PlayerJoinRaw.PlayerJoinRawBuilder;
import eu.solven.kumite.player.PlayerMoveRaw.PlayerMoveRawBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
public class PlayerMovesHandler {
	final GamesRegistry gamesStore;
	final ContestsRegistry contestsStore;

	final BoardLifecycleManager boardLifecycleManager;

	public Mono<ServerResponse> registerPlayer(ServerRequest request) {
		PlayerJoinRawBuilder parameters = PlayerJoinRaw.builder();

		UUID playerId = KumiteHandlerHelper.uuid(request, "player_id");
		parameters.playerId(playerId);

		UUID contestId = KumiteHandlerHelper.uuid(request, "contest_id");

		Optional<Boolean> optViewer = KumiteHandlerHelper.optBoolean(request, "viewer");
		optViewer.ifPresent(viewer -> parameters.isViewer(viewer));

		Contest contest = contestsStore.getContest(contestId);

		PlayerJoinRaw playerJoinRaw = parameters.build();
		boardLifecycleManager.registerPlayer(contest, playerJoinRaw);

		Map<String, ?> output =
				Map.of("playerId", playerId, "contestId", contestId, "viewer", playerJoinRaw.isViewer());
		return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(output));
	}

	public Mono<ServerResponse> registerPlayerMove(ServerRequest request) {
		PlayerMoveRawBuilder parameters = PlayerMoveRaw.builder();

		UUID playerId = KumiteHandlerHelper.uuid(request, "player_id");
		parameters.playerId(playerId);

		UUID contestId = KumiteHandlerHelper.uuid(request, "contest_id");
		// parameters.contestId(contestId);

		Contest contest = contestsStore.getContest(contestId);
		IGame game = gamesStore.getGame(contest.getGameMetadata().getGameId());

		return request.bodyToMono(Map.class).<ServerResponse>flatMap(rawMove -> {
			IKumiteMove move = game.parseRawMove(rawMove);

			parameters.move(move);

			PlayerMoveRaw playerMove = PlayerMoveRaw.builder().playerId(playerId).move(move).build();
			boardLifecycleManager.onPlayerMove(contest, playerMove);

			Map<String, UUID> output = Map.of("playerId", playerId, "contestId", contestId);
			return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(output));
		});
	}

	// This would return a list of possible moves. The list may not be exhaustive, but indicative.
	public Mono<ServerResponse> listPlayerMoves(ServerRequest request) {
		PlayerMoveRawBuilder parameters = PlayerMoveRaw.builder();

		UUID playerId = KumiteHandlerHelper.uuid(request, "player_id");
		parameters.playerId(playerId);

		UUID contestId = KumiteHandlerHelper.uuid(request, "contest_id");
		// parameters.contestId(contestId);

		Contest contest = contestsStore.getContest(contestId);
		IGame game = gamesStore.getGame(contest.getGameMetadata().getGameId());

		IKumiteBoardView boardView = contest.getBoard().get().asView(playerId);
		Map<String, IKumiteMove> moves = game.exampleMoves(boardView, playerId);

		return ServerResponse.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromValue(Map.of("moves", moves)));
	}
}