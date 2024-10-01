package eu.solven.kumite.account.fake_player;

import eu.solven.kumite.account.KumiteUserDetails;
import eu.solven.kumite.account.KumiteUserRawRaw;
import eu.solven.kumite.account.internal.KumiteUser;
import eu.solven.kumite.account.internal.KumiteUserPreRegister;

public class FakeUser {

	public static KumiteUserRawRaw rawRaw() {
		return KumiteUserRawRaw.builder().providerId("kumite").sub("fakeSub").build();
	}

	public static KumiteUserPreRegister pre() {
		KumiteUserDetails details =
				KumiteUserDetails.builder().username("fakeUsername").email("fake@fake").name("Fake User").build();
		return KumiteUserPreRegister.builder().rawRaw(rawRaw()).details(details).build();
	}

	public static KumiteUser user() {
		KumiteUserPreRegister pre = pre();
		return KumiteUser.builder()
				.accountId(FakePlayer.ACCOUNT_ID)
				.playerId(FakePlayer.PLAYER_ID1)
				.rawRaw(pre.getRawRaw())
				.details(pre.getDetails())
				.build();
	}

}
