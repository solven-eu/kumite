package eu.solven.kumite.board;

import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import eu.solven.kumite.contest.KumiteHandlerHelper;
import eu.solven.kumite.game.optimization.tsp.IKumiteBoardView;
import lombok.Value;
import reactor.core.publisher.Mono;

@Value
public class BoardHandler {

	BoardsRegistry boardsRegistry;

	public Mono<ServerResponse> getBoard(ServerRequest request) {
		UUID contestId = KumiteHandlerHelper.uuid(request.queryParam("contest_id"));
		UUID playerId = KumiteHandlerHelper.uuid(request.queryParam("player_id"));

		IKumiteBoard board = boardsRegistry.makeDynamicBoardHolder(contestId).get();
		IKumiteBoardView boardView = board.asView(playerId);

		return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(boardView));
	}
}