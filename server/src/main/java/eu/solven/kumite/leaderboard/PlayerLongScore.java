package eu.solven.kumite.leaderboard;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Builder
@Value
@Jacksonized
public class PlayerLongScore implements IPlayerScore {
	UUID playerId;
	long score;

	@JsonIgnore
	@Override
	public double getComparableScore() {
		return score;
	}
}
