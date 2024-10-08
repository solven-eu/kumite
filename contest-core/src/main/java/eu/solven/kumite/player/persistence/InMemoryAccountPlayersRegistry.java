package eu.solven.kumite.player.persistence;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

import eu.solven.kumite.account.fake_player.FakePlayer;
import eu.solven.kumite.account.fake_player.RandomPlayer;
import eu.solven.kumite.exception.UnknownPlayerException;
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
	}

	@Override
	public void registerPlayer(KumitePlayer player) {
		UUID accountId = player.getAccountId();

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
	public Optional<UUID> optAccountId(UUID playerId) {
		return Optional.ofNullable(playerIdToAccountId.get(playerId));
	}

	@Override
	public UUID getAccountId(UUID playerId) {
		return optAccountId(playerId).orElseThrow(() -> new UnknownPlayerException(playerId));
	}

	@Override
	public IHasPlayers makeDynamicHasPlayers(UUID accountId) {
		return () -> accountToPlayers.getOrDefault(accountId, Set.of())
				.stream()
				.sorted()
				.map(playerId -> KumitePlayer.builder().playerId(playerId).accountId(accountId).build())
				.collect(Collectors.toList());
	}

	@Override
	public UUID generateMainPlayerId(UUID accountId) {
		if (FakePlayer.ACCOUNT_ID.equals(accountId)) {
			return FakePlayer.PLAYER_ID1;
		} else if (RandomPlayer.ACCOUNT_ID.equals(accountId)) {
			return RandomPlayer.PLAYERID_1;
		}

		return uuidGenerator.randomUUID();
	}
}
