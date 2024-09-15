package eu.solven.kumite.player;

import java.util.UUID;

public interface IAccountPlayersRegistry {

	void registerPlayer(UUID accountId, KumitePlayer player);

	UUID getAccountId(UUID playerId);

	IHasPlayers makeDynamicHasPlayers(UUID accountId);

	UUID generatePlayerId(UUID accountId);

}
