package eu.solven.kumite.account.fake_player;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

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
 * actual login flow, with an external login provider.
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
		springApplication.setAdditionalProfiles(IKumiteSpringProfiles.P_FAKE_USER);

		Map<String, Object> defaultProperties = new LinkedHashMap<>();
		// We set a quite long expiry as this is typically injected as default token in application-fake_player.yml
		// defaultProperties.put(KumiteTokenService.KEY_ACCESSTOKEN_EXP, );

		springApplication.setDefaultProperties(defaultProperties);

		springApplication.run().close();
	}

	@Bean
	public Void generateFakePlayerToken(KumiteTokenService tokenService) {
		String accessToken = tokenService.generateAccessToken(FakePlayerTokens.fakeUser(),
				FakePlayerTokens.FAKE_PLAYER_ID1,
				Duration.ofDays(365));

		log.info("access_token for fakeUser: {}", accessToken);

		return null;
	}

}
