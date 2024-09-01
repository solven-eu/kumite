package eu.solven.kumite.board;

import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import eu.solven.kumite.contest.KumiteHandlerHelper;
import lombok.Value;
import reactor.core.publisher.Mono;

@Value
public class BoardHandler {
	BoardsRegistry boardsRegistry;

	public Mono<ServerResponse> getBoard(ServerRequest request) {
		UUID contestId = KumiteHandlerHelper.uuid(request.queryParam("contest_id"));

		return ServerResponse.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromValue(boardsRegistry.makeDynamicBoardHolder(contestId).get()));
	}
}