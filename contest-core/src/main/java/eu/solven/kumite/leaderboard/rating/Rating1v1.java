package eu.solven.kumite.leaderboard.rating;

import java.util.UUID;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * A rating specialized for 1v1 games.
 * 
 * @author Benoit Lacelle
 *
 */
@Value
@Builder
@Jacksonized
public class Rating1v1 {
	@NonNull
	UUID playerId;
	@NonNull
	UUID gameId;

	@Default
	int wins = 0;
	@Default
	int loses = 0;
	@Default
	int ties = 0;

	public Rating1v1 markWin() {
		return Rating1v1.builder().gameId(gameId).playerId(playerId).wins(wins + 1).loses(loses).ties(ties).build();
	}

	public Rating1v1 markLos() {
		return Rating1v1.builder().gameId(gameId).playerId(playerId).wins(wins).loses(loses + 1).ties(ties).build();
	}

	public Rating1v1 markTie() {
		return Rating1v1.builder().gameId(gameId).playerId(playerId).wins(wins).loses(loses).ties(ties + 1).build();
	}
}
