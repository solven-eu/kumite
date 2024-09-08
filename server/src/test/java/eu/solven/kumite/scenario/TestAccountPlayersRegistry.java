package eu.solven.kumite.scenario;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import eu.solven.kumite.account.KumiteUser;
import eu.solven.kumite.game.GamesRegistry;
import eu.solven.kumite.player.AccountPlayersRegistry;
import eu.solven.kumite.player.KumitePlayer;

public class TestAccountPlayersRegistry {
	GamesRegistry gamesRegistry = new GamesRegistry();
	AccountPlayersRegistry playersRegistry = new AccountPlayersRegistry(gamesRegistry);

	@Test
	public void testFakePlayer() {
		Assertions.assertThat(playersRegistry.getAccountId(KumitePlayer.FAKE_PLAYER_ID))
				.isEqualTo(KumiteUser.FAKE_ACCOUNT_ID);
	}
}
