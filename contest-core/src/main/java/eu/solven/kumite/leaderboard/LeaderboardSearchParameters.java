package eu.solven.kumite.leaderboard;

import java.util.UUID;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class LeaderboardSearchParameters {
	@NonNull
	UUID contestId;

	// // By default, we look for leaderboard around the best rank
	// @Default
	// OptionalInt rank = OptionalInt.of(1);
	//
	// // Do we look for ranks around a playerId?
	// @Default
	// Optional<UUID> playerId = Optional.empty();
}
