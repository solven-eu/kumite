package eu.solven.kumite.account.login;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import eu.solven.kumite.account.KumiteUsersRegistry;
import eu.solven.kumite.account.internal.KumiteUser;
import eu.solven.kumite.account.internal.KumiteUserPreRegister;
import eu.solven.kumite.app.IKumiteSpringProfiles;
import eu.solven.kumite.app.persistence.InMemoryKumiteConfiguration;
import eu.solven.kumite.tools.JdkUuidGenerator;
import lombok.extern.slf4j.Slf4j;

@ExtendWith(SpringExtension.class)
@Import({ JdkUuidGenerator.class,

		InMemoryKumiteConfiguration.class,

		KumiteUsersRegistry.class, })

@ActiveProfiles({ IKumiteSpringProfiles.P_INMEMORY })
@Slf4j
public class TestKumiteUsersRegistry implements IKumiteTestConstants {
	@Autowired
	KumiteUsersRegistry usersRegistry;

	@Test
	public void testUnknownUser() {
		Assertions.assertThatThrownBy(() -> usersRegistry.getUser(someAccountId))
				.isInstanceOf(IllegalArgumentException.class);
		Assertions.assertThatThrownBy(() -> usersRegistry.getAccountMainPlayer(somePlayerId))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void testRegisterUser() {
		KumiteUserPreRegister raw = IKumiteTestConstants.userPreRegister();
		KumiteUser registered = usersRegistry.registerOrUpdate(raw);

		Assertions.assertThat(usersRegistry.getUser(registered.getAccountId())).isEqualTo(registered);
		Assertions.assertThat(usersRegistry.getAccountMainPlayer(registered.getAccountId()))
				.isEqualTo(registered.mainPlayer());
	}
}
