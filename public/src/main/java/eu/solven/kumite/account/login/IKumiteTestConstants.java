package eu.solven.kumite.account.login;

import java.util.UUID;

import eu.solven.kumite.account.KumiteUserDetails;
import eu.solven.kumite.account.KumiteUserRawRaw;
import eu.solven.kumite.account.internal.KumiteUserPreRegister;

public interface IKumiteTestConstants {
	String PROVIDERID_TEST = "test";

	UUID someAccountId = UUID.randomUUID();
	UUID somePlayerId = UUID.randomUUID();
	UUID someContestId = UUID.randomUUID();

	static KumiteUserRawRaw userRawRaw() {
		return KumiteUserRawRaw.builder().providerId(PROVIDERID_TEST).sub("test").build();
	}

	static KumiteUserDetails userDetails() {
		return KumiteUserDetails.builder().username("fakeUsername").build();
	}

	static KumiteUserPreRegister userPreRegister() {
		return KumiteUserPreRegister.builder().rawRaw(userRawRaw()).details(userDetails()).build();
	}
}
