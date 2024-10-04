package eu.solven.kumite.events;

import java.util.UUID;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * Request a contest gameOver: the contest is (probably) not gameOver yet about about to be gameOver.
 * 
 * @author Benoit Lacelle
 *
 */
@Value
@Builder
public class ForceContestGameover {
	@NonNull
	UUID contestId;
}
