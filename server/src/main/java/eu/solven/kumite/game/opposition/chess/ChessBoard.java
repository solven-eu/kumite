package eu.solven.kumite.game.opposition.chess;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import eu.solven.kumite.board.IKumiteBoard;
import eu.solven.kumite.game.optimization.tsp.IKumiteBoardView;
import eu.solven.kumite.player.PlayerMoveRaw;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class ChessBoard implements IKumiteBoard, IKumiteBoardView {
	// https://en.wikipedia.org/wiki/Portable_Game_Notation
	String pgn;

	@Override
	public List<String> isValidMove(PlayerMoveRaw playerMove) {
		return Collections.singletonList("TODO");
	}

	@Override
	public void registerMove(PlayerMoveRaw playerMove) {
		throw new IllegalArgumentException("TODO");
	}

	@Override
	public IKumiteBoardView asView(UUID playerId) {
		// Every players can see the whole board
		return this;
	}
}
