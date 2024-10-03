package eu.solven.kumite.events;

import java.util.UUID;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * Used by `turn-based` contest, to notify next player being able to play.
 * 
 * @author Benoit Lacelle
 *
 */
@Value
@Builder
public class PlayerCanMove implements IKumiteContestEvent {
	@NonNull
	UUID contestId;

	@NonNull
	UUID playerId;
}
