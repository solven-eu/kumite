package eu.solven.kumite.leaderboard;

import java.util.UUID;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Builder
@Value
@Jacksonized
public class PlayerDoubleScore implements IPlayerScore {
	UUID playerId;
	double score;

	@Override
	public Number asNumber() {
		return score;
	}
}
