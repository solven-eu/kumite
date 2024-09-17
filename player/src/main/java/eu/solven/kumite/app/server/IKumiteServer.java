package eu.solven.kumite.app.server;

import java.util.Map;
import java.util.UUID;

import eu.solven.kumite.contest.ContestMetadataRaw;
import eu.solven.kumite.contest.ContestSearchParameters;
import eu.solven.kumite.contest.ContestView;
import eu.solven.kumite.game.GameMetadata;
import eu.solven.kumite.game.GameSearchParameters;
import eu.solven.kumite.leaderboard.LeaderboardRaw;
import eu.solven.kumite.player.PlayerContestStatus;
import eu.solven.kumite.player.PlayerRawMovesHolder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Wraps the gaming API
 * 
 * @author Benoit Lacelle
 *
 */
public interface IKumiteServer {
	Flux<GameMetadata> searchGames(GameSearchParameters search);

	Flux<ContestMetadataRaw> searchContests(ContestSearchParameters contestSearchParameters);

	Mono<ContestView> loadBoard(UUID playerId, UUID contestId);

	Mono<PlayerContestStatus> joinContest(UUID playerId, UUID contestId);

	Mono<PlayerRawMovesHolder> getExampleMoves(UUID playerId, UUID contestId);

	// We may want not to receive the board, for optimization reasons.
	Mono<ContestView> playMove(UUID playerId, UUID contestId, Map<String, ?> move);

	Mono<LeaderboardRaw> loadLeaderboard(UUID contestId);

}
