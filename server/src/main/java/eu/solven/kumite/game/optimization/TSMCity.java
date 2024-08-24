package eu.solven.kumite.game.optimization;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TSMCity {
	String name;

	double x;
	double y;
}
