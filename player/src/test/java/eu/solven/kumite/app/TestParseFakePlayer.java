package eu.solven.kumite.app;

import java.util.Set;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import lombok.extern.slf4j.Slf4j;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = { KumitePlayerApplication.class }, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles({
		// Enables generation on-the-fly on refreshToken
		IKumiteSpringProfiles.P_UNSAFE_OAUTH2,
		// Enables playing as fakeUser
		IKumiteSpringProfiles.P_FAKEUSER,
		IKumiteSpringProfiles.P_UNSAFE_PLAYER })
@TestPropertySource(properties = { KumiteWebclientServerProperties.KEY_PLAYER_CONTESTBASEURL + "=someUrl",
		KumiteWebclientServerProperties.ENV_REFRESH_TOKEN + "="
				+ KumiteWebclientServerProperties.PLACEHOLDER_GENERATEFAKEPLAYER })
@Slf4j
public class TestParseFakePlayer implements IKumiteSpringProfiles {

	@Autowired
	Environment env;

	@Autowired
	KumitePlayerComponentsConfiguration conf;

	@Autowired
	KumiteWebclientServerProperties kumiteWebclientServerProperties;

	@Test
	public void testPlayerIdFromAccessToken() {
		Set<UUID> playerIds = conf.playerIdFromRefreshToken(kumiteWebclientServerProperties);

		Assertions.assertThat(playerIds)
				.contains(UUID.fromString("11111111-1111-1111-1111-111111111111"))
				.contains(UUID.fromString("11111111-1111-1111-1111-222222222222"))
				.hasSize(2);
	}
}
