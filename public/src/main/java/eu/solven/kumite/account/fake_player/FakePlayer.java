package eu.solven.kumite.account.fake_player;

import java.util.Set;
import java.util.UUID;

import eu.solven.kumite.player.KumitePlayer;
import lombok.extern.slf4j.Slf4j;

/**
 * Various tools specific to the FakePlayer. This player is useful for local development, circumventing the need for an
 * actual login flow, with an external login provider.
 * 
 * @author Benoit Lacelle
 *
 */
@Slf4j
public class FakePlayer {

	// IKumiteSpringProfiles.P_DEFAULT_FAKE_USER
	public static final UUID ACCOUNT_ID = UUID.fromString("11111111-1111-1111-1111-000000000000");
	public static final UUID PLAYER_ID1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
	public static final UUID PLAYER_ID2 = UUID.fromString("11111111-1111-1111-1111-222222222222");

	public static UUID fakePlayerId(int playerIndex) {
		if (playerIndex == 0) {
			return PLAYER_ID1;
		} else if (playerIndex == 1) {
			return PLAYER_ID2;
		} else {
			throw new IllegalArgumentException("There is no fakePlayer for playerIndex=" + playerIndex);
		}
	}

	public static boolean isFakePlayer(UUID playerId) {
		if (PLAYER_ID1.equals(playerId) || PLAYER_ID2.equals(playerId)) {
			return true;
		} else {
			return false;
		}
	}

	public static KumitePlayer fakePlayer() {
		return KumitePlayer.builder().playerId(PLAYER_ID1).accountId(ACCOUNT_ID).build();
	}

	public static KumitePlayer player(int i) {
		return KumitePlayer.builder().playerId(fakePlayerId(i)).accountId(ACCOUNT_ID).build();
	}

	public static Set<UUID> playersIds() {
		return Set.of(FakePlayer.PLAYER_ID1, FakePlayer.PLAYER_ID2);
	}

}
