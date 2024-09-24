package eu.solven.kumite.player;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import eu.solven.kumite.account.fake_player.FakePlayer;
import eu.solven.kumite.player.persistence.InMemoryAccountPlayersRegistry;
import eu.solven.kumite.tools.JdkUuidGenerator;

public class TestInMemoryAccountPlayersRegistry {
	IAccountPlayersRegistry playersRegistry = new InMemoryAccountPlayersRegistry(new JdkUuidGenerator());

	private void registerFakePlayers() {
		playersRegistry.registerPlayer(FakePlayer.fakePlayer());
		playersRegistry.registerPlayer(FakePlayer.player(1));
	}

	@Test
	public void testFakePlayer() {
		registerFakePlayers();

		Assertions.assertThat(playersRegistry.getAccountId(FakePlayer.PLAYER_ID1)).isEqualTo(FakePlayer.ACCOUNT_ID);
		Assertions.assertThat(playersRegistry.getAccountId(FakePlayer.PLAYER_ID2)).isEqualTo(FakePlayer.ACCOUNT_ID);
	}

	@Test
	public void testHasPlayers_FakePlayers() {
		registerFakePlayers();

		List<KumitePlayer> players = playersRegistry.makeDynamicHasPlayers(FakePlayer.ACCOUNT_ID).getPlayers();

		Assertions.assertThat(players)
				.contains(
						KumitePlayer.builder().playerId(FakePlayer.PLAYER_ID1).accountId(FakePlayer.ACCOUNT_ID).build())
				.contains(KumitePlayer.builder()
						.playerId(FakePlayer.PLAYER_ID2)
						.accountId(FakePlayer.ACCOUNT_ID)
						.build())
				.hasSize(2);
	}
}
