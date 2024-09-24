package eu.solven.kumite.player;

import java.util.List;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import eu.solven.kumite.account.fake_player.FakePlayer;
import eu.solven.kumite.player.persistence.BijectiveAccountPlayersRegistry;

public class TestBijectiveAccountPlayersRegistry {
	IAccountPlayersRegistry playersRegistry = new BijectiveAccountPlayersRegistry();

	@Test
	public void testFakePlayer() {
		Assertions.assertThat(playersRegistry.getAccountId(FakePlayer.PLAYER_ID1))
				.isEqualTo(FakePlayer.ACCOUNT_ID);
		Assertions.assertThat(playersRegistry.getAccountId(FakePlayer.PLAYER_ID2))
				.isEqualTo(FakePlayer.ACCOUNT_ID);
	}

	@Test
	public void testNormalPlayer() {
		UUID accountId = UUID.fromString("b9337740-6f18-4d8f-b611-8d03428151c3");
		UUID playerId = playersRegistry.generateMainPlayerId(accountId);
		Assertions.assertThat(playerId).hasToString("3c151824-30d8-116b-f8d4-81f60477339b");

		KumitePlayer player = KumitePlayer.builder().playerId(playerId).accountId(accountId).build();
		playersRegistry.registerPlayer(player);

		Assertions.assertThat(playersRegistry.getAccountId(playerId)).isEqualTo(accountId);
		Assertions.assertThat(playersRegistry.makeDynamicHasPlayers(accountId).getPlayers())
				.contains(player)
				.hasSize(1);
	}

	@Test
	public void testHasPlayers_FakePlayers() {
		List<KumitePlayer> players =
				playersRegistry.makeDynamicHasPlayers(FakePlayer.ACCOUNT_ID).getPlayers();

		Assertions.assertThat(players)
				.contains(KumitePlayer.builder()
						.playerId(FakePlayer.PLAYER_ID1)
						.accountId(FakePlayer.ACCOUNT_ID)
						.build())
				.contains(KumitePlayer.builder()
						.playerId(FakePlayer.PLAYER_ID2)
						.accountId(FakePlayer.ACCOUNT_ID)
						.build())
				.hasSize(2);
	}
}
