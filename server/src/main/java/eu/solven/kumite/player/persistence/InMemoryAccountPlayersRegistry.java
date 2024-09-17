package eu.solven.kumite.player.persistence;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

import eu.solven.kumite.account.fake_player.FakePlayerTokens;
import eu.solven.kumite.player.IAccountPlayersRegistry;
import eu.solven.kumite.player.IHasPlayers;
import eu.solven.kumite.player.KumitePlayer;
import eu.solven.kumite.tools.IUuidGenerator;

/**
 * An in-memory implementation of {@link IAccountPlayersRegistry}
 * 
 * @author Benoit Lacelle
 *
 */
public final class InMemoryAccountPlayersRegistry implements IAccountPlayersRegistry {
	final Map<UUID, Set<UUID>> accountToPlayers = new ConcurrentHashMap<>();
	final Map<UUID, UUID> playerIdToAccountId = new ConcurrentHashMap<>();

	final IUuidGenerator uuidGenerator;

	public InMemoryAccountPlayersRegistry(IUuidGenerator uuidGenerator) {
		this.uuidGenerator = uuidGenerator;

		registerPlayer(FakePlayerTokens.FAKE_ACCOUNT_ID, FakePlayerTokens.fakePlayer());
		registerPlayer(FakePlayerTokens.FAKE_ACCOUNT_ID, FakePlayerTokens.fakePlayer());
	}

	@Override
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

	@Override
	public UUID getAccountId(UUID playerId) {
		if (FakePlayerTokens.isFakePlayer(playerId)) {
			return FakePlayerTokens.FAKE_ACCOUNT_ID;
		}

		UUID accountId = playerIdToAccountId.get(playerId);
		if (accountId == null) {
			throw new IllegalArgumentException("Unknown playerId: " + playerId);
		}
		return accountId;
	}

	@Override
	public IHasPlayers makeDynamicHasPlayers(UUID accountId) {
		return () -> accountToPlayers.getOrDefault(accountId, Set.of())
				.stream()
				.sorted()
				// playerName?
				.map(playerId -> KumitePlayer.builder().playerId(playerId).build())
				.collect(Collectors.toList());
	}

	@Override
	public UUID generateMainPlayerId(UUID accountId) {
		return uuidGenerator.randomUUID();
	}
}
