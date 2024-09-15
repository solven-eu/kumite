package eu.solven.kumite.player;

import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import eu.solven.kumite.account.KumiteUser;
import eu.solven.kumite.player.persistence.BijectiveAccountPlayersRegistry;

public class TestBijectiveAccountPlayersRegistry {
	IAccountPlayersRegistry playersRegistry = new BijectiveAccountPlayersRegistry();

	@Test
	public void testFakePlayer() {
		Assertions.assertThat(playersRegistry.getAccountId(KumitePlayer.FAKE_PLAYER_ID))
				.isEqualTo(KumiteUser.FAKE_ACCOUNT_ID);
	}

	@Test
	public void testNormalPlayer() {
		UUID accountId = UUID.fromString("b9337740-6f18-4d8f-b611-8d03428151c3");
		UUID playerId = playersRegistry.generatePlayerId(accountId);
		Assertions.assertThat(playerId).hasToString("3c151824-30d8-116b-f8d4-81f60477339b");

		playersRegistry.registerPlayer(accountId, KumitePlayer.fromPlayerId(playerId));

		Assertions.assertThat(playersRegistry.getAccountId(playerId)).isEqualTo(accountId);
		Assertions.assertThat(playersRegistry.makeDynamicHasPlayers(accountId).getPlayers())
				.contains(KumitePlayer.fromPlayerId(playerId))
				.hasSize(1);
	}
}
