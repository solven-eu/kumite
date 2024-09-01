package eu.solven.kumite.game.optimization.tsp;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TSPCity {
	String name;

	double x;
	double y;
}