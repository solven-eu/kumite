package eu.solven.kumite.account.login;

import java.util.UUID;

import eu.solven.kumite.account.KumiteUserRaw;
import eu.solven.kumite.account.KumiteUserRawRaw;
import eu.solven.kumite.app.webflux.api.KumiteLoginController;

public interface IKumiteTestConstants {
	UUID someAccountId = UUID.randomUUID();
	UUID somePlayerId = UUID.randomUUID();

	static KumiteUserRaw userRaw() {
		KumiteUserRawRaw rawRaw =
				KumiteUserRawRaw.builder().providerId(KumiteLoginController.PROVIDERID_TEST).sub("test").build();
		return KumiteUserRaw.builder().rawRaw(rawRaw).email("test@test").username("fakeUsername").build();
	}
}
