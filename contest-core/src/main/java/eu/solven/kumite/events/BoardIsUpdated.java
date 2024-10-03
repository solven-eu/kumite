package eu.solven.kumite.events;

import java.util.UUID;

import eu.solven.kumite.contest.Contest;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * A {@link Contest} board has been updated. Typically, as player joined or moved.
 * 
 * @author Benoit Lacelle
 *
 */
@Value
@Builder
public class BoardIsUpdated implements IKumiteContestEvent {
	@NonNull
	UUID contestId;
}
