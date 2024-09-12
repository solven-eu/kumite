package eu.solven.kumite.player;

import java.util.UUID;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * A participating player and its status relatively to a contest.
 * 
 * @author Benoit Lacelle
 *
 */
@Value
@Builder
@Jacksonized
public class PlayerContestStatus {
	@NonNull
	UUID playerId;

	// We may introduce a contenderId. Through it is unclear how to manage it given this may also represent a viewer, or
	// a previewer

	// Has joined as a contender, not a viewer
	boolean playerHasJoined;
	// Can join as a contender
	boolean playerCanJoin;
	// Any player own by the account is viewing
	boolean accountIsViewing;

	public static PlayerContestStatus contender(UUID playerId) {
		return PlayerContestStatus.builder().playerId(playerId).playerHasJoined(true).build();
	}

	public static PlayerContestStatus viewer(UUID playerId) {
		return PlayerContestStatus.builder().playerId(playerId).accountIsViewing(true).build();
	}
}
