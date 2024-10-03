package eu.solven.kumite.events;

import java.util.UUID;

import eu.solven.kumite.contest.Contest;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * A {@link Contest} switched from being active to being gameover.
 * 
 * @author Benoit Lacelle
 *
 */
@Value
@Builder
public class ContestIsGameover implements IKumiteContestEvent {
	@NonNull
	UUID contestId;
}
