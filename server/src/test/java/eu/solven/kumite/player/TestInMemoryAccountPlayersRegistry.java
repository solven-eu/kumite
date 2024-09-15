package eu.solven.kumite.player;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import eu.solven.kumite.account.KumiteUser;
import eu.solven.kumite.player.persistence.InMemoryAccountPlayersRegistry;
import eu.solven.kumite.tools.JdkUuidGenerator;

public class TestInMemoryAccountPlayersRegistry {
	IAccountPlayersRegistry playersRegistry = new InMemoryAccountPlayersRegistry(new JdkUuidGenerator());

	@Test
	public void testFakePlayer() {
		Assertions.assertThat(playersRegistry.getAccountId(KumitePlayer.FAKE_PLAYER_ID))
				.isEqualTo(KumiteUser.FAKE_ACCOUNT_ID);
	}
}
