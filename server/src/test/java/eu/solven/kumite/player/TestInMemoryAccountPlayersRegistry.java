package eu.solven.kumite.player;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import eu.solven.kumite.account.fake_player.FakePlayerTokens;
import eu.solven.kumite.player.persistence.InMemoryAccountPlayersRegistry;
import eu.solven.kumite.tools.JdkUuidGenerator;

public class TestInMemoryAccountPlayersRegistry {
	IAccountPlayersRegistry playersRegistry = new InMemoryAccountPlayersRegistry(new JdkUuidGenerator());

	@Test
	public void testFakePlayer() {
		Assertions.assertThat(playersRegistry.getAccountId(FakePlayerTokens.FAKE_PLAYER_ID1))
				.isEqualTo(FakePlayerTokens.FAKE_ACCOUNT_ID);
		Assertions.assertThat(playersRegistry.getAccountId(FakePlayerTokens.FAKE_PLAYER_ID2))
				.isEqualTo(FakePlayerTokens.FAKE_ACCOUNT_ID);
	}
}
