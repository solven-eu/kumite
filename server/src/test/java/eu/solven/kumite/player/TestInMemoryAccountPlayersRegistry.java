package eu.solven.kumite.player;

import java.util.List;

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

	@Test
	public void testHasPlayers_FakePlayers() {
		List<KumitePlayer> players =
				playersRegistry.makeDynamicHasPlayers(FakePlayerTokens.FAKE_ACCOUNT_ID).getPlayers();

		Assertions.assertThat(players)
				.contains(KumitePlayer.builder()
						.playerId(FakePlayerTokens.FAKE_PLAYER_ID1)
						.accountId(FakePlayerTokens.FAKE_ACCOUNT_ID)
						.build())
				.contains(KumitePlayer.builder()
						.playerId(FakePlayerTokens.FAKE_PLAYER_ID2)
						.accountId(FakePlayerTokens.FAKE_ACCOUNT_ID)
						.build())
				.hasSize(2);
	}
}
