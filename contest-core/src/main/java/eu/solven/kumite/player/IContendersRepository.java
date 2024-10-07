package eu.solven.kumite.player;

import java.util.UUID;

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
	 * @return the new boardStateId
	 */
	UUID registerContender(UUID contestId, UUID playerId);

	boolean isContender(UUID contestId, UUID playerId);

	IHasPlayers hasPlayers(UUID contestId);

	void gameover(UUID contestId);

	long getContestIds(UUID playerId);

}
