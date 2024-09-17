package eu.solven.kumite.contest;

import java.util.Set;
import java.util.UUID;

import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * The set of metadata describing the evolving state of a contest, independently of current player.
 * 
 * @author Benoit Lacelle
 *
 */
@Value
@Builder
@Jacksonized
public class ContestDynamicMetadata {

	// Should we return the actual list of players?
	@NonNull
	@Singular
	Set<UUID> contenders;

	boolean acceptingPlayers;
	boolean requiringPlayers;

	boolean gameOver;

}
