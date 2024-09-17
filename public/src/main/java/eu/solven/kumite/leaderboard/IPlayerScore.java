package eu.solven.kumite.leaderboard;

import java.util.UUID;

import eu.solven.kumite.contest.ContestMetadataRaw;

/**
 * Represent the score of a player for a given {@link ContestMetadataRaw}.
 * 
 * @author Benoit Lacelle
 *
 */
public interface IPlayerScore {
	UUID getPlayerId();

	/**
	 * 
	 * @return a way to order score for leaderboards. Some scores may not be easily converted into aone-dimensional
	 *         double.
	 */
	Number asNumber();
}
