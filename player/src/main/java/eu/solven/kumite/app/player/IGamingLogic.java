package eu.solven.kumite.app.player;

import java.util.Set;
import java.util.UUID;

/**
 * Wraps high-level operations for a contender.
 * 
 * @author Benoit Lacelle
 *
 */
public interface IGamingLogic {

	/**
	 * Optimization games are the simplest one in term of integration: one just have to publish one move/solution to get
	 * on the leaderboard.
	 * 
	 * @param kumiteServer
	 * @param playerId
	 * @return 
	 */
	Set<UUID> playOptimizationGames(UUID playerId);

	/**
	 * 1v1 games needs a `do-while` loop, to play moves until the game is over.
	 * 
	 * @param kumiteServer
	 * @param playerId
	 */
	Set<UUID> play1v1TurnBasedGames(UUID playerId);

}
