package eu.solven.kumite.account.fake_player;

import java.util.Set;
import java.util.UUID;

import eu.solven.kumite.account.KumiteUser;
import eu.solven.kumite.account.KumiteUserRaw;
import eu.solven.kumite.account.KumiteUserRawRaw;
import eu.solven.kumite.player.KumitePlayer;
import lombok.extern.slf4j.Slf4j;

/**
 * Various tools specific to the RandomPlayer. This player is useful to generate activity even for a PRD contest-server,
 * circumventing the need for an actual login flow, with an external login provider.
 * 
 * @author Benoit Lacelle
 *
 */
@Slf4j
public class RandomPlayer {

	public static final UUID ACCOUNT_ID = UUID.fromString("FFFFFFFF-FFFF-FFFF-FFFF-000000000000");
	public static final UUID PLAYERID_1 = UUID.fromString("FFFFFFFF-FFFF-FFFF-FFFF-111111111111");
	public static final UUID RANDOM_PLAYERID2 = UUID.fromString("FFFFFFFF-FFFF-FFFF-FFFF-222222222222");

	public static UUID randomPlayerId(int playerIndex) {
		if (playerIndex == 0) {
			return PLAYERID_1;
		} else if (playerIndex == 1) {
			return RANDOM_PLAYERID2;
		} else {
			throw new IllegalArgumentException("There is no randomPlayer for playerIndex=" + playerIndex);
		}
	}

	public static boolean isRandomPlayer(UUID playerId) {
		if (PLAYERID_1.equals(playerId) || RANDOM_PLAYERID2.equals(playerId)) {
			return true;
		} else {
			return false;
		}
	}

	public static KumiteUser user() {
		KumiteUserRawRaw rawRaw = KumiteUserRawRaw.builder().providerId("kumite").sub("randomSub").build();
		KumiteUserRaw raw = KumiteUserRaw.builder()
				.rawRaw(rawRaw)
				.username("randomUsername")
				.email("random@random")
				.name("Random User")
				.build();
		return KumiteUser.builder().accountId(ACCOUNT_ID).playerId(PLAYERID_1).raw(raw).build();
	}

	public static KumitePlayer randomPlayer() {
		return KumitePlayer.builder().playerId(PLAYERID_1).accountId(ACCOUNT_ID).build();
	}

	public static KumitePlayer randomPlayer(int i) {
		return KumitePlayer.builder().playerId(randomPlayerId(i)).accountId(ACCOUNT_ID).build();
	}

	public static Set<UUID> randomPlayers() {
		return Set.of(RandomPlayer.PLAYERID_1, RandomPlayer.RANDOM_PLAYERID2);
	}

}
