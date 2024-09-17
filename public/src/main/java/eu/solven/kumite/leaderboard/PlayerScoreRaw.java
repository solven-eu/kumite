package eu.solven.kumite.leaderboard;

import java.util.UUID;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

// https://services.docs.unity.com/leaderboards/#tag/Leaderboards/operation/getLeaderboardScores
@Value
@Builder
@Jacksonized
public class PlayerScoreRaw {
	UUID playerId;
	int rank;
	Number score;
}
