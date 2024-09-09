package eu.solven.kumite.app;

import eu.solven.kumite.contest.ContestView;
import eu.solven.kumite.game.GameMetadata;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IKumiteServer {
	Flux<GameMetadata> searchGames();

	Flux<GameMetadata> searchContests();

	Mono<ContestView> loadBoard();
}
