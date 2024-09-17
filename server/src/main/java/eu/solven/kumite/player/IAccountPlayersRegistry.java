package eu.solven.kumite.player;

import java.util.UUID;

/**
 * Tracks the playerIds owned by an accountId.
 * 
 * @author Benoit Lacelle
 *
 */
public interface IAccountPlayersRegistry {

	void registerPlayer(UUID accountId, KumitePlayer player);

	UUID getAccountId(UUID playerId);

	IHasPlayers makeDynamicHasPlayers(UUID accountId);

	/**
	 * 
	 * @param accountId
	 * @return a valid playerId to be used as the account main playerId
	 */
	UUID generateMainPlayerId(UUID accountId);

}
