package eu.solven.kumite.player;

import java.util.UUID;

import eu.solven.kumite.board.IKumiteBoard;

/**
 * Access to the playingPlayers/contenders of a contest.
 * 
 * @author Benoit Lacelle
 *
 */
public interface IContendersRepository {

	/**
	 * 
	 * @param contestId
	 * @param playerId
	 * @return true if this call took in charge the registration in the {@link IKumiteBoard}.
	 */
	boolean registerContender(UUID contestId, UUID playerId);

	boolean isContender(UUID contestId, UUID playerId);

	IHasPlayers makeDynamicHasPlayers(UUID contestId);

	void gameover(UUID contestId);

	long getContestIds(UUID playerId);

}
