package eu.solven.kumite.player;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

import eu.solven.kumite.account.KumiteUser;
import eu.solven.kumite.account.login.FakePlayerTokens;

public final class AccountPlayersRegistry {
	final Map<UUID, Set<UUID>> accountToPlayers = new ConcurrentHashMap<>();
	final Map<UUID, UUID> playerIdToAccountId = new ConcurrentHashMap<>();

	public AccountPlayersRegistry() {
		registerPlayer(KumiteUser.FAKE_ACCOUNT_ID, FakePlayerTokens.fakePlayer());
	}

	public void registerPlayer(UUID accountId, KumitePlayer player) {
		// Synchronized to make atomic changes to both `accountToPlayers` and `playerIdToAccountId`
		synchronized (this) {
			UUID accountIdAlreadyIn = playerIdToAccountId.putIfAbsent(player.getPlayerId(), accountId);
			if (accountIdAlreadyIn != null && !accountIdAlreadyIn.equals(accountId)) {
				throw new IllegalArgumentException(
						"player=" + player + " is already owned by account=" + accountIdAlreadyIn);
			}

			accountToPlayers.computeIfAbsent(accountId, k -> new ConcurrentSkipListSet<>()).add(player.getPlayerId());
		}
	}

	public UUID getAccountId(UUID playerId) {
		if (KumitePlayer.FAKE_PLAYER_ID.equals(playerId)) {
			return KumiteUser.FAKE_ACCOUNT_ID;
		}

		UUID accountId = playerIdToAccountId.get(playerId);
		if (accountId == null) {
			throw new IllegalArgumentException("Unknown playerId: " + playerId);
		}
		return accountId;
	}

	public IHasPlayers makeDynamicHasPlayers(UUID accountId) {
		return () -> accountToPlayers.getOrDefault(accountId, Set.of())
				.stream()
				.sorted()
				// playerName?
				.map(playerId -> KumitePlayer.builder().playerId(playerId).build())
				.collect(Collectors.toList());
	}
}
