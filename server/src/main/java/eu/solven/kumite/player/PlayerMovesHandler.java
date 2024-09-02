package eu.solven.kumite.player;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import eu.solven.kumite.contest.Contest;
import eu.solven.kumite.contest.ContestsRegistry;
import eu.solven.kumite.contest.KumiteHandlerHelper;
import eu.solven.kumite.game.GamesRegistry;
import eu.solven.kumite.game.IGame;
import eu.solven.kumite.game.optimization.tsp.IKumiteBoardView;
import eu.solven.kumite.lifecycle.BoardLifecycleManager;
import eu.solven.kumite.player.PlayerMoveRaw.PlayerMoveRawBuilder;
import lombok.Value;
import reactor.core.publisher.Mono;

@Value
public class PlayerMovesHandler {
	GamesRegistry gamesStore;
	ContestsRegistry contestsStore;

	BoardLifecycleManager boardLifecycleManager;

	public Mono<ServerResponse> registerPlayerMove(ServerRequest request) {
		PlayerMoveRawBuilder parameters = PlayerMoveRaw.builder();

		UUID playerId = KumiteHandlerHelper.uuid(request, "player_id");
		parameters.playerId(playerId);

		UUID contestId = KumiteHandlerHelper.uuid(request, "contest_id");
		// parameters.contestId(contestId);

		Contest contest = contestsStore.getContest(contestId);
		IGame game = gamesStore.getGame(contest.getContestMetadata().getGameMetadata().getGameId());

		return request.bodyToMono(Map.class).<ServerResponse>flatMap(rawMove -> {
			IKumiteMove move = game.parseRawMove(rawMove);

			parameters.move(move);

			PlayerMoveRaw playerMove = PlayerMoveRaw.builder().playerId(playerId).move(move).build();
			boardLifecycleManager.onPlayerMove(contest, playerMove);

			return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(Map.of()));
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
		IGame game = gamesStore.getGame(contest.getContestMetadata().getGameMetadata().getGameId());

		IKumiteBoardView boardView = contest.getBoard().get().asView(playerId);
		Map<String, IKumiteMove> moves = game.exampleMoves(boardView, playerId);

		return ServerResponse.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromValue(Map.of("moves", moves)));
	}
}