package eu.solven.kumite.game.optimization.tsp;

import java.util.List;

import eu.solven.kumite.board.IKumiteBoard;
import eu.solven.kumite.player.KumitePlayer;
import eu.solven.kumite.player.PlayerMoveRaw;

/**
 * A {@link IKumiteBoardView} is a (optionally partial) view of an {@link IKumiteBoard} for a given {@link KumitePlayer}
 * 
 * @author Benoit Lacelle
 *
 */
public interface IKumiteBoardView {

	List<String> isValidMove(PlayerMoveRaw playerMove);

}
