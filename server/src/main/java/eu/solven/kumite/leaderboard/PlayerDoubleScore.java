package eu.solven.kumite.leaderboard;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Builder
@Value
@Jacksonized
public class PlayerDoubleScore implements IPlayerScore {
	UUID playerId;
	double score;

	@JsonIgnore
	@Override
	public double getComparableScore() {
		return score;
	}
}
