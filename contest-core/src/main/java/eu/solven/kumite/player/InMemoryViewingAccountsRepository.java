package eu.solven.kumite.player;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import lombok.NonNull;

public class InMemoryViewingAccountsRepository implements IViewingAccountsRepository {

	// Once a player is viewing, it can not play as it got some private information about the game.
	// The public information of a board is available by querying the board with playerId=KumitePlayer.PUBLIC
	// A cheater could use 2 accounts: one to look at public information, the other to actually play the game
	final Map<UUID, Set<UUID>> contestToViewingAccounts = new ConcurrentHashMap<>();

	@Override
	public void registerViewer(@NonNull UUID contestId, UUID accountId) {
		contestToViewingAccounts.computeIfAbsent(contestId, k -> new ConcurrentSkipListSet<>()).add(accountId);
	}

	@Override
	public boolean isViewing(UUID contestId, UUID accountId) {
		return contestToViewingAccounts.getOrDefault(contestId, Collections.emptySet()).contains(accountId);
	}
}
