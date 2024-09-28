package eu.solven.kumite.events;

import java.util.UUID;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * A contest is created, and players can join.
 * 
 * @author Benoit Lacelle
 *
 */
@Value
@Builder
public class ContestIsCreated {
	@NonNull
	UUID contestId;
}
