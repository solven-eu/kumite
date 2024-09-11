package eu.solven.kumite.app;

import java.util.Map;
import java.util.UUID;

import eu.solven.kumite.contest.ContestMetadataRaw;
import eu.solven.kumite.contest.ContestSearchParameters;
import eu.solven.kumite.contest.ContestView;
import eu.solven.kumite.game.GameMetadata;
import eu.solven.kumite.game.GameSearchParameters;
import eu.solven.kumite.player.PlayerRawMovesHolder;
import eu.solven.kumite.player.PlayingPlayer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IKumiteServer {
	Flux<GameMetadata> searchGames(GameSearchParameters search);

	Flux<ContestMetadataRaw> searchContests(ContestSearchParameters contestSearchParameters);

	Mono<ContestView> loadBoard(UUID contestId, UUID playerId);

	Mono<PlayingPlayer> joinContest(UUID playerId, UUID contestId);

	Mono<PlayerRawMovesHolder> getExampleMoves(UUID playerId, UUID contestId);

	Mono<ContestView> playMove(UUID playerId, UUID contestId, Map<String, ?> move);

}
