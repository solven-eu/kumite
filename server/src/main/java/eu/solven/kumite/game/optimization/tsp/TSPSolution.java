package eu.solven.kumite.game.optimization.tsp;

import java.util.List;

import eu.solven.kumite.player.IKumiteMove;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class TSPSolution implements IKumiteMove {
	// The ordered list of cities to visit
	@Singular
	List<String> cities;
}
