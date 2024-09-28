package eu.solven.kumite.leaderboard.rating;

import java.util.UUID;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PlayerAtGame {
	UUID gameId;

	UUID playerId;
}
