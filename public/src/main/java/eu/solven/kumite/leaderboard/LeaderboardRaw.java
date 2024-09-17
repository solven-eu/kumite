package eu.solven.kumite.leaderboard;

import java.util.List;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class LeaderboardRaw {
	// This should be ordered at creation
	List<? extends PlayerScoreRaw> playerScores;

}
