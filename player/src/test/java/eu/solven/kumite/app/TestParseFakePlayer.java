package eu.solven.kumite.app;

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
@ActiveProfiles({ IKumiteSpringProfiles.P_FAKE_USER })
@TestPropertySource(properties = "kumite.server.base-url" + "=someUrl")
@Slf4j
public class TestParseFakePlayer implements IKumiteSpringProfiles {

	@Autowired
	Environment env;

	@Autowired
	KumitePlayerComponentsConfiguration conf;

	@Test
	public void testPlayerIdFromAccessToken() {
		UUID playerId = conf.playerIdFromAccessToken(env);

		Assertions.assertThat(playerId).isEqualTo(UUID.fromString("11111111-1111-1111-1111-111111111111"));
	}
}
