package eu.solven.kumite.player;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import eu.solven.kumite.contest.Contest;
import eu.solven.kumite.contest.ContestsRegistry;
import eu.solven.kumite.game.GamesRegistry;
import eu.solven.kumite.game.IGame;
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

		Optional<String> optPlayerId = request.queryParam("player_id");
		UUID playerId =
				UUID.fromString(optPlayerId.orElseThrow(() -> new IllegalArgumentException("Lack `player_id`")));
		parameters.playerId(playerId);

		Optional<String> optContestId = request.queryParam("contest_id");
		UUID contestId =
				UUID.fromString(optContestId.orElseThrow(() -> new IllegalArgumentException("Lack `contest_id`")));
		parameters.contestId(contestId);

		Contest contest = contestsStore.getContest(contestId);
		IGame game = gamesStore.getGame(contest.getContestMetadata().getGameMetadata().getGameId());

		return request.bodyToMono(Map.class).<ServerResponse>flatMap(rawMove -> {
			IKumiteMove move = game.parseRawMove(rawMove);

			parameters.move(move);

			PlayerMove playerMove = PlayerMove.builder().contestId(contestId).playerId(playerId).move(move).build();
			boardLifecycleManager.onPlayerMove(contest, playerMove);

			return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(Map.of()));
		});

	}
}