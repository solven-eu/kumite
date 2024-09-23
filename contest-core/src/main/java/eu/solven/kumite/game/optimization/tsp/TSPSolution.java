package eu.solven.kumite.game.optimization.tsp;

import java.util.List;

import eu.solven.kumite.move.IKumiteMove;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class TSPSolution implements IKumiteMove {
	// This is useful to register players not having submitted a solution yet
	public static final TSPSolution EMPTY = TSPSolution.builder().build();

	// The ordered list of cities to visit
	@Singular
	List<String> cities;
}
