package eu.solven.kumite.player.persistence;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import eu.solven.kumite.account.fake_player.FakePlayer;
import eu.solven.kumite.account.fake_player.RandomPlayer;
import eu.solven.kumite.player.IAccountPlayersRegistry;
import eu.solven.kumite.player.IHasPlayers;
import eu.solven.kumite.player.KumitePlayer;
import lombok.extern.slf4j.Slf4j;

/**
 * This implementation enforces each account has a single playerId, and a computable bijection between accountId and
 * playerId.
 * 
 * @author Benoit Lacelle
 *
 */
@Slf4j
public final class BijectiveAccountPlayersRegistry implements IAccountPlayersRegistry {

	@Override
	public void registerPlayer(KumitePlayer player) {
		UUID accountId = player.getAccountId();
		UUID playerId = player.getPlayerId();
		if (FakePlayer.ACCOUNT_ID.equals(accountId) && FakePlayer.isFakePlayer(playerId)) {
			log.info("Registering a fakePlayer={}", playerId);
		} else if (RandomPlayer.ACCOUNT_ID.equals(accountId) && RandomPlayer.isRandomPlayer(playerId)) {
			log.info("Registering a randomPlayer={}", playerId);
		} else if (playerId.equals(generateMainPlayerId(accountId))) {
			log.info("Registering accountId={} playerId={}", accountId, playerId);
		} else {
			throw new IllegalArgumentException("Invalid playerId=" + playerId + " given accountId=" + accountId);
		}
	}

	@Override
	public UUID getAccountId(UUID playerId) {
		if (FakePlayer.isFakePlayer(playerId)) {
			return FakePlayer.ACCOUNT_ID;
		} else if (RandomPlayer.isRandomPlayer(playerId)) {
			return RandomPlayer.ACCOUNT_ID;
		}

		return accountIdGivenPlayerId(playerId);
	}

	@Override
	public IHasPlayers makeDynamicHasPlayers(UUID accountId) {
		if (FakePlayer.ACCOUNT_ID.equals(accountId)) {
			List<KumitePlayer> players = Arrays.asList(FakePlayer.player(0), FakePlayer.player(1));
			return () -> players;
		} else if (RandomPlayer.ACCOUNT_ID.equals(accountId)) {
			List<KumitePlayer> players = Arrays.asList(RandomPlayer.player(0), RandomPlayer.player(1));
			return () -> players;
		}

		// The playerId can not change as it is derived from the accountId only
		UUID playerId = generateMainPlayerId(accountId);

		return () -> Collections.singletonList(KumitePlayer.builder().playerId(playerId).accountId(accountId).build());
	}

	@Override
	public UUID generateMainPlayerId(UUID accountId) {
		if (FakePlayer.ACCOUNT_ID.equals(accountId)) {
			return FakePlayer.PLAYER_ID1;
		} else if (RandomPlayer.ACCOUNT_ID.equals(accountId)) {
			return RandomPlayer.PLAYERID_1;
		}

		return playerIdGivenAccountId(accountId);
	}

	// Each account has a default/main playerId. It is useful to have an injection accountId -> playerId as it helps
	// skipping the storage of this information. We do not pick accountId as playerId as it could be confusive to gave
	// the same id for different things.
	public static UUID playerIdGivenAccountId(UUID accountId) {
		String accountIdAsString = accountId.toString();
		// https://stackoverflow.com/questions/7569335/reverse-a-string-in-java
		String reversedAccountIdAsString = new StringBuilder(accountIdAsString).reverse().toString();

		// "02df90d8-e7aa-4b07-8a70-6f8cb358e9bb"
		String playerIdAsString = reversedAccountIdAsString.substring(0, 8) + "-"
				+ reversedAccountIdAsString.substring(8, 12)
				+ "-"
				+ reversedAccountIdAsString.substring(13, 17)
				+ "-"
				+ reversedAccountIdAsString.substring(18, 22)
				+ "-"
				+ reversedAccountIdAsString.substring(23, 27)
				+ reversedAccountIdAsString.substring(28, 36);
		return UUID.fromString(playerIdAsString);
	}

	public static UUID accountIdGivenPlayerId(UUID playerId) {
		return playerIdGivenAccountId(playerId);
	}
}
