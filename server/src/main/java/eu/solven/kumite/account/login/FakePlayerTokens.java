package eu.solven.kumite.account.login;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import eu.solven.kumite.account.KumiteUser;
import eu.solven.kumite.account.KumiteUserRaw;
import eu.solven.kumite.account.KumiteUserRawRaw;
import eu.solven.kumite.app.IKumiteSpringProfiles;
import eu.solven.kumite.app.KumiteRandomConfiguration;
import eu.solven.kumite.player.KumitePlayer;
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
public class FakePlayerTokens {

	public static void main(String[] args) {
		SpringApplication springApplication = new SpringApplication(FakePlayerTokens.class);

		springApplication.setWebApplicationType(WebApplicationType.NONE);
		springApplication.setAdditionalProfiles(IKumiteSpringProfiles.P_FAKE_PLAYER);

		Map<String, Object> defaultProperties = new LinkedHashMap<>();
		// We set a quite long expiry as this is typically injected as default token in application-fake_player.yml
		defaultProperties.put(KumiteTokenService.KEY_ACCESSTOKEN_EXP, Duration.ofDays(365).toString());

		springApplication.setDefaultProperties(defaultProperties);

		springApplication.run().close();
	}

	@Bean
	public Void generateFakePlayerToken(KumiteTokenService tokenService) {
		String accessToken = tokenService.generateAccessToken(fakeUser(), KumitePlayer.FAKE_PLAYER_ID);

		log.info("access_token for fakeUser: {}", accessToken);

		return null;
	}

	public static KumiteUser fakeUser() {
		KumiteUserRawRaw rawRaw = KumiteUserRawRaw.builder().providerId("fakeProviderId").sub("fakeSub").build();
		KumiteUserRaw raw = KumiteUserRaw.builder()
				.rawRaw(rawRaw)
				.username("fakeUsername")
				.email("fake@fake")
				.name("Fake User")
				.build();
		return KumiteUser.builder()
				.accountId(KumiteUser.FAKE_ACCOUNT_ID)
				.playerId(KumitePlayer.FAKE_PLAYER_ID)
				.raw(raw)
				.build();
	}

	public static KumitePlayer fakePlayer() {
		return KumitePlayer.builder().playerId(KumitePlayer.FAKE_PLAYER_ID).build();
	}

}
