package eu.solven.kumite.game.snake;

import eu.solven.kumite.move.IKumiteMove;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * A snake move is a change of direction of the snake head.
 * 
 * @author Benoit Lacelle
 *
 */
@Value
@Builder
@Jacksonized
public class SnakeMove implements IKumiteMove {
	// Like a clock:
	// 0 goes up
	// 3 goes right
	// 6 goes down
	// 9 goes left
	int direction;
}
