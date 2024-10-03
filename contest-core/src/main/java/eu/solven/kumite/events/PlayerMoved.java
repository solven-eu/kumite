package eu.solven.kumite.events;

import java.util.UUID;

import eu.solven.kumite.contest.Contest;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * A {@link Contest} board has been updated due to player move.
 * 
 * This may be relevant only for `turn-based` games.
 * 
 * @author Benoit Lacelle
 *
 */
@Value
@Builder
public class PlayerMoved implements IKumiteContestEvent {
	@NonNull
	UUID contestId;

	@NonNull
	UUID playerId;
}
