package eu.solven.kumite.game.opposition.chess;

import eu.solven.kumite.player.IKumiteMove;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class ChessMove implements IKumiteMove {
	// The move in pgn notation
	// https://en.wikipedia.org/wiki/Portable_Game_Notation
	String move;
}
