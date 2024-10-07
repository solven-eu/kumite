package eu.solven.kumite.board;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.solven.kumite.move.PlayerMoveRaw;

/**
 * Needs to be serializable to {@link String} by {@link ObjectMapper} as it is stored in Redis as json.
 * 
 * @author Benoit Lacelle
 *
 */
public interface IKumiteBoard {
	// Some games may need to register some player parameters
	void registerContender(UUID playerId);


	/**
	 * Mutate current board with given player move
	 * 
	 * @param playerMove
	 */
	void registerMove(PlayerMoveRaw playerMove);

	/**
	 * 
	 * @param playerId
	 * @return a {@link IKumiteBoardView} of current board for given playerId. This is especially relevant for games
	 *         with imperfect information.
	 */
	IKumiteBoardView asView(UUID playerId);

	List<UUID> snapshotContenders();
}
