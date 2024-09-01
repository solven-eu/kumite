package eu.solven.kumite.game.opposition.chess;

import java.util.Collections;
import java.util.List;

import eu.solven.kumite.board.IKumiteBoard;
import eu.solven.kumite.player.PlayerMove;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ChessBoard implements IKumiteBoard {
	// https://en.wikipedia.org/wiki/Portable_Game_Notation
	String pgn;

	@Override
	public List<String> isValidMove(PlayerMove playerMove) {
		return Collections.singletonList("TODO");
	}

	@Override
	public void registerMove(PlayerMove playerMove) {
		throw new IllegalArgumentException("TODO");
	}
}
