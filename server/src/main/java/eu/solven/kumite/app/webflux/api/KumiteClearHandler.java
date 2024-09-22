package eu.solven.kumite.app.webflux.api;

import java.util.Map;

import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import eu.solven.kumite.board.persistence.InMemoryBoardRepository;
import eu.solven.kumite.contest.persistence.InMemoryContestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
public class KumiteClearHandler {
	// We reset only in-memory structures, to prevent a scenario resetting Redis
	final InMemoryContestRepository contestRepository;
	final InMemoryBoardRepository boardRepository;

	public Mono<ServerResponse> clear(ServerRequest request) {
		contestRepository.clear();
		boardRepository.clear();

		return KumiteHandlerHelper.okAsJson(Map.of("clear", true));
	}
}
