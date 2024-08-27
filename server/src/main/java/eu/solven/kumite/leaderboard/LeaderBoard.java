package eu.solven.kumite.leaderboard;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Value;

@Value
@Builder
public class LeaderBoard {
	@Default
	Map<UUID, IPlayerScore> playerIdToPlayerScore = new ConcurrentHashMap<>();

	public void registerScore(IPlayerScore playerScore) {
		playerIdToPlayerScore.put(playerScore.getPlayerId(), playerScore);
	}

	public static LeaderBoard empty() {
		return new LeaderBoard(Map.of());
	}
}
