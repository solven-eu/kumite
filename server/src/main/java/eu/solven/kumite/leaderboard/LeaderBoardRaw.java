package eu.solven.kumite.leaderboard;

import java.util.List;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class LeaderBoardRaw {
	// This should be ordered at creation
	List<? extends IPlayerScore> playerScores;

}
