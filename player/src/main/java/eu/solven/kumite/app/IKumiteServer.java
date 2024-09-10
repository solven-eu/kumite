package eu.solven.kumite.app;

import java.util.UUID;

import eu.solven.kumite.contest.ContestMetadataRaw;
import eu.solven.kumite.contest.ContestSearchParameters;
import eu.solven.kumite.contest.ContestView;
import eu.solven.kumite.game.GameMetadata;
import eu.solven.kumite.game.GameSearchParameters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IKumiteServer {
	Flux<GameMetadata> searchGames(GameSearchParameters search);

	Flux<ContestMetadataRaw> searchContests(ContestSearchParameters contestSearchParameters);

	Mono<ContestView> loadBoard(UUID contestId, UUID playerId);

	Mono<ContestView> joinContest(UUID playerId, UUID contestId);

}
