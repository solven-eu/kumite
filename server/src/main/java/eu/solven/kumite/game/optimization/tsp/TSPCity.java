package eu.solven.kumite.game.optimization.tsp;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class TSPCity {
	String name;

	double x;
	double y;
}
