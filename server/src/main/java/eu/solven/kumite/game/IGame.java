package eu.solven.kumite.game;

import java.util.Map;

import eu.solven.kumite.board.IKumiteBoard;
import eu.solven.kumite.player.IKumiteMove;

public interface IGame {
	GameMetadata getGameMetadata();

	boolean isValidMove(IKumiteMove move);

	IKumiteMove parseRawMove(Map<String, ?> rawMove);

	IKumiteBoard generateInitialBoard();
}
