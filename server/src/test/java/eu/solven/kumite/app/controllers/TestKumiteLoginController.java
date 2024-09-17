package eu.solven.kumite.app.controllers;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import eu.solven.kumite.account.fake_player.FakePlayerTokens;
import eu.solven.kumite.account.login.KumiteTokenService;
import eu.solven.kumite.account.login.KumiteUsersRegistry;
import eu.solven.kumite.player.IAccountPlayersRegistry;
import eu.solven.kumite.player.persistence.BijectiveAccountPlayersRegistry;
import eu.solven.kumite.tools.IUuidGenerator;
import eu.solven.kumite.tools.JdkUuidGenerator;
import eu.solven.kumite.user.InMemoryUserRepository;

public class TestKumiteLoginController {
	final ClientRegistration someClientRegistration = ClientRegistration.withRegistrationId("someRegistrationId")
			.authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
			.clientId("someClientId")
			.tokenUri("someTokenUri")
			.build();

	final InMemoryReactiveClientRegistrationRepository clientRegistrationRepository =
			new InMemoryReactiveClientRegistrationRepository(someClientRegistration);

	final IUuidGenerator uuidGenerator = new JdkUuidGenerator();

	final IAccountPlayersRegistry playersRegistry = new BijectiveAccountPlayersRegistry();
	final InMemoryUserRepository userRepository = new InMemoryUserRepository(uuidGenerator, playersRegistry);

	final KumiteUsersRegistry usersRegistry = new KumiteUsersRegistry(userRepository, userRepository);
	final Environment env = new MockEnvironment();

	final KumiteTokenService kumiteTokenService = new KumiteTokenService(env, uuidGenerator);

	final KumiteLoginController controller = new KumiteLoginController(clientRegistrationRepository,
			usersRegistry,
			playersRegistry,
			env,
			kumiteTokenService);

	@Test
	public void testPlayer_invalid() {
		Assertions
				.assertThatThrownBy(
						() -> controller.checkValidPlayerId(FakePlayerTokens.fakeUser(), uuidGenerator.randomUUID()))
				.isInstanceOf(IllegalArgumentException.class);
	}
}
