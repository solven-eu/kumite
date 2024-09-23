package eu.solven.kumite.account.login;

import java.util.UUID;

import eu.solven.kumite.account.KumiteUserRaw;
import eu.solven.kumite.account.KumiteUserRawRaw;

public interface IKumiteTestConstants {
	String PROVIDERID_TEST = "test";

	UUID someAccountId = UUID.randomUUID();
	UUID somePlayerId = UUID.randomUUID();
	UUID someContestId = UUID.randomUUID();

	static KumiteUserRaw userRaw() {
		KumiteUserRawRaw rawRaw = KumiteUserRawRaw.builder().providerId(PROVIDERID_TEST).sub("test").build();
		return KumiteUserRaw.builder().rawRaw(rawRaw).email("test@test").username("fakeUsername").build();
	}
}
