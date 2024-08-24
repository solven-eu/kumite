package eu.solven.kumite.contest;

import eu.solven.kumite.board.BoardAndPlayers;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Contest {
	ContestMetadata contestMetadata;

	// The board would change through time and/or userInteractions
	BoardAndPlayers refBoard;
}
