package eu.solven.kumite.game.optimization;

import java.util.List;

import eu.solven.kumite.player.IKumiteMove;
import lombok.Value;

@Value
public class TSMSolution implements IKumiteMove {
	// The ordered list of cities to visit
	List<String> cities;
}
