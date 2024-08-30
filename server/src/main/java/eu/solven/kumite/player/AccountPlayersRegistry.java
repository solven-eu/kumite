package eu.solven.kumite.player;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

import eu.solven.kumite.game.GamesStore;
import lombok.Value;

@Value
public class AccountPlayersRegistry {
	GamesStore gamesStore;

	Map<UUID, Set<UUID>> accountToPlayers = new ConcurrentHashMap<>();
	Map<UUID, UUID> playerIdToAccountId = new ConcurrentHashMap<>();

	public void registerPlayer(UUID accountId, KumitePlayer player) {
		// Synchronized to make atomic changes to both `accountToPlayers` and `playerIdToAccountId`
		synchronized (this) {
			UUID accountIdAlreadyIn = playerIdToAccountId.putIfAbsent(player.getPlayerId(), accountId);
			if (!accountIdAlreadyIn.equals(accountId)) {
				throw new IllegalArgumentException(
						"player=" + player + " is already owned by account=" + accountIdAlreadyIn);
			}

			accountToPlayers.computeIfAbsent(accountId, k -> new ConcurrentSkipListSet<>()).add(player.getPlayerId());
		}
	}

	public IHasPlayers makeDynamicHasPlayers(UUID accountId) {
		return () -> accountToPlayers.getOrDefault(accountId, Set.of())
				.stream()
				.sorted()
				// playerName?
				.map(playerId -> KumitePlayer.builder().playerId(playerId).build())
				.collect(Collectors.toList());
	}

	// public boolean isRegisteredPlayer(UUID accountId, UUID playerId) {
	// return accountToPlayers.getOrDefault(accountId, Set.of()).contains(playerId);
	// }
}
