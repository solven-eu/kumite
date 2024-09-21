package eu.solven.kumite.account.fake_player;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import eu.solven.kumite.account.login.KumiteJwtSigningConfiguration;
import eu.solven.kumite.account.login.KumiteTokenService;
import eu.solven.kumite.app.IKumiteSpringProfiles;
import eu.solven.kumite.app.KumiteRandomConfiguration;
import lombok.extern.slf4j.Slf4j;

/**
 * Various tools specific to the FakePlayer. This player is useful for local development, circumventing the need for an
 * actual login flow, with an external login provider. It is generally not useful as we rely on `GENERATE_FAKEUSER`
 * placeholder to generate a token on the fly.
 * 
 * @author Benoit Lacelle
 *
 */
@SpringBootApplication(scanBasePackages = "none")
@Import({ KumiteJwtSigningConfiguration.class, KumiteRandomConfiguration.class })
@Slf4j
public class RunFakePlayerToken {

	public static void main(String[] args) {
		SpringApplication springApplication = new SpringApplication(RunFakePlayerToken.class);

		springApplication.setWebApplicationType(WebApplicationType.NONE);
		springApplication.setAdditionalProfiles(
				// unsafe-server provide a signingKey
				IKumiteSpringProfiles.P_UNSAFE_SERVER,
				// fake_user tells the fakeUser is usable
				IKumiteSpringProfiles.P_FAKE_USER);

		Map<String, Object> defaultProperties = new LinkedHashMap<>();
		springApplication.setDefaultProperties(defaultProperties);

		springApplication.run().close();
	}

	@Bean
	public Void generateFakePlayerToken(KumiteTokenService tokenService) {
		String accessToken = tokenService.generateAccessToken(FakePlayerTokens.fakeUser(),
				Set.of(FakePlayerTokens.FAKE_PLAYER_ID1, FakePlayerTokens.FAKE_PLAYER_ID2),
				Duration.ofDays(365),
				false);

		log.info("access_token for fakeUser: {}", accessToken);

		return null;
	}

}
