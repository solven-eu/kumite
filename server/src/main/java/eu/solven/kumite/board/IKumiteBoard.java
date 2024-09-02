package eu.solven.kumite.board;

import java.util.List;
import java.util.UUID;

import eu.solven.kumite.game.optimization.tsp.IKumiteBoardView;
import eu.solven.kumite.player.PlayerMoveRaw;

public interface IKumiteBoard {

	List<String> isValidMove(PlayerMoveRaw playerMove);

	void registerMove(PlayerMoveRaw playerMove);

	IKumiteBoardView asView(UUID playerId);
}
