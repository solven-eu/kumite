package eu.solven.kumite.leaderboard.rating;

import java.util.UUID;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * A rating specialized for multiplayer games.
 * 
 * @author Benoit Lacelle
 *
 */
@Value
@Builder
@Jacksonized
public class RatingMultiplayer {
	private static final int BUCKETS = 8;

	@NonNull
	UUID playerId;
	@NonNull
	UUID gameId;

	// Even if the game was actually played with 100 players, we would increment the rank based on the quantile.
	@Default
	int[] rankWins = new int[BUCKETS];

	public RatingMultiplayer markRank(int rank, int nbPlayers) {
		int[] updatedRankWins = rankWins.clone();

		int normalizedRankWin = (rank * BUCKETS) / nbPlayers;

		updatedRankWins[normalizedRankWin] = rankWins[normalizedRankWin] + 1;

		return RatingMultiplayer.builder().gameId(gameId).playerId(playerId).rankWins(updatedRankWins).build();
	}
}
