package eu.solven.kumite.player;

import java.util.UUID;

/**
 * Access to the playingPlayers/contenders of a contest.
 * 
 * @author Benoit Lacelle
 *
 */
public interface IContendersRepository {

	boolean registerContender(UUID contestId, UUID playerId);

	boolean isContender(UUID contestId, UUID playerId);

	IHasPlayers makeDynamicHasPlayers(UUID contestId);

}
