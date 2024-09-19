package eu.solven.kumite.game.optimization.lag;

import eu.solven.kumite.move.IKumiteMove;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class LagServerTimestamp implements IKumiteMove {
	// Typically generated by the server, we evaluate the lag for this date to go to the player and being sent back
	String moveTimestamp;
}
