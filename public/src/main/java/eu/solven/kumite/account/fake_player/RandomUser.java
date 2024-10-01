package eu.solven.kumite.account.fake_player;

import eu.solven.kumite.account.KumiteUserDetails;
import eu.solven.kumite.account.KumiteUserRawRaw;
import eu.solven.kumite.account.internal.KumiteUser;
import eu.solven.kumite.account.internal.KumiteUserPreRegister;

public class RandomUser {

	public static KumiteUserRawRaw rawRaw() {
		return KumiteUserRawRaw.builder().providerId("kumite").sub("randomSub").build();
	}

	public static KumiteUserPreRegister pre() {
		KumiteUserDetails details = KumiteUserDetails.builder()
				.username("randomUsername")
				.email("random@random")
				.name("Random User")
				.build();
		return KumiteUserPreRegister.builder().rawRaw(rawRaw()).details(details).build();
	}

	public static KumiteUser user() {
		KumiteUserPreRegister pre = pre();
		return KumiteUser.builder()
				.accountId(RandomPlayer.ACCOUNT_ID)
				.playerId(RandomPlayer.PLAYERID_1)
				.rawRaw(pre.getRawRaw())
				.details(pre.getDetails())
				.build();
	}

}
