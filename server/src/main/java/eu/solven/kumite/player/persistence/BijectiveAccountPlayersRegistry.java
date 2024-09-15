package eu.solven.kumite.player.persistence;

import java.util.Collections;
import java.util.UUID;

import eu.solven.kumite.account.KumiteUser;
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
	public void registerPlayer(UUID accountId, KumitePlayer player) {
		UUID playerId = player.getPlayerId();
		if (accountId.equals(KumiteUser.FAKE_ACCOUNT_ID) && playerId.equals(KumitePlayer.FAKE_PLAYER_ID)) {
			log.info("Registering the fakeUser");
		} else if (playerId.equals(generatePlayerId(accountId))) {
			log.info("Registering accountId={} playerId={}", accountId, playerId);
		} else {
			throw new IllegalArgumentException("Invalid playerId=" + playerId + " given accountId=" + accountId);
		}
	}

	@Override
	public UUID getAccountId(UUID playerId) {
		if (playerId.equals(KumitePlayer.FAKE_PLAYER_ID)) {
			return KumiteUser.FAKE_ACCOUNT_ID;
		}

		return accountIdGivenPlayerId(playerId);
	}

	@Override
	public IHasPlayers makeDynamicHasPlayers(UUID accountId) {
		UUID playerId = generatePlayerId(accountId);

		return () -> Collections.singletonList(KumitePlayer.builder().playerId(playerId).build());
	}

	@Override
	public UUID generatePlayerId(UUID accountId) {
		if (accountId.equals(KumiteUser.FAKE_ACCOUNT_ID)) {
			return KumitePlayer.FAKE_PLAYER_ID;
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
