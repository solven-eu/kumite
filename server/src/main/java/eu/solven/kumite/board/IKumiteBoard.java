package eu.solven.kumite.board;

import java.util.List;
import java.util.UUID;

import eu.solven.kumite.player.PlayerMoveRaw;

public interface IKumiteBoard {
	// Some games may need to register some player parameters
	void registerPlayer(UUID playerId);

	List<String> isValidMove(PlayerMoveRaw playerMove);

	void registerMove(PlayerMoveRaw playerMove);

	IKumiteBoardView asView(UUID playerId);
}
