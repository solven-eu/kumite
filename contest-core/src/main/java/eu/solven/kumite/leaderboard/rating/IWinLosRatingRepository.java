package eu.solven.kumite.leaderboard.rating;

import java.util.UUID;

public interface IWinLosRatingRepository {

	void markWin(UUID gameId, UUID playerId);

	void markLos(UUID gameId, UUID playerId);

	void markTie(UUID gameId, UUID playerId);

	void markRank(UUID gameId, UUID key, int rank, int nbPlayers);
}
