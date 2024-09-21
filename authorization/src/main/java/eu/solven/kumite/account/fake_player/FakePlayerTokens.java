package eu.solven.kumite.account.fake_player;

import java.util.Set;
import java.util.UUID;

import eu.solven.kumite.account.KumiteUser;
import eu.solven.kumite.account.KumiteUserRaw;
import eu.solven.kumite.account.KumiteUserRawRaw;
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
public class FakePlayerTokens {

	// IKumiteSpringProfiles.P_DEFAULT_FAKE_USER
	public static final UUID FAKE_ACCOUNT_ID = UUID.fromString("11111111-1111-1111-1111-000000000000");
	public static final UUID FAKE_PLAYER_ID1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
	public static final UUID FAKE_PLAYER_ID2 = UUID.fromString("11111111-1111-1111-1111-222222222222");

	public static UUID fakePlayerId(int playerIndex) {
		if (playerIndex == 0) {
			return FAKE_PLAYER_ID1;
		} else if (playerIndex == 1) {
			return FAKE_PLAYER_ID2;
		} else {
			throw new IllegalArgumentException("There is no fakePlayer for playerIndex=" + playerIndex);
		}
	}

	public static boolean isFakePlayer(UUID playerId) {
		if (FAKE_PLAYER_ID1.equals(playerId) || FAKE_PLAYER_ID2.equals(playerId)) {
			return true;
		} else {
			return false;
		}
	}

	public static KumiteUser fakeUser() {
		KumiteUserRawRaw rawRaw = KumiteUserRawRaw.builder().providerId("fakeProviderId").sub("fakeSub").build();
		KumiteUserRaw raw = KumiteUserRaw.builder()
				.rawRaw(rawRaw)
				.username("fakeUsername")
				.email("fake@fake")
				.name("Fake User")
				.build();
		return KumiteUser.builder().accountId(FAKE_ACCOUNT_ID).playerId(FAKE_PLAYER_ID1).raw(raw).build();
	}

	public static KumitePlayer fakePlayer() {
		return KumitePlayer.builder().playerId(FAKE_PLAYER_ID1).accountId(FAKE_ACCOUNT_ID).build();
	}

	public static KumitePlayer fakePlayer(int i) {
		return KumitePlayer.builder().playerId(fakePlayerId(i)).accountId(FAKE_ACCOUNT_ID).build();
	}

	public static Set<UUID> fakePlayers() {
		return Set.of(FakePlayerTokens.FAKE_PLAYER_ID1, FakePlayerTokens.FAKE_PLAYER_ID2);
	}

}
