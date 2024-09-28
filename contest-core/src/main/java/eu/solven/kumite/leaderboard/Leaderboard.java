package eu.solven.kumite.leaderboard;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Value;

/**
 * A {@link Leaderboard} tracks the score of players for a given game, through contests.
 * 
 * @author Benoit Lacelle
 *
 */
@Value
@Builder
public class Leaderboard {
	@Default
	Map<UUID, IPlayerScore> playerIdToPlayerScore = new ConcurrentHashMap<>();

	public void registerScore(IPlayerScore playerScore) {
		playerIdToPlayerScore.put(playerScore.getPlayerId(), playerScore);
	}

	public static Leaderboard empty() {
		return new Leaderboard(Map.of());
	}
}
