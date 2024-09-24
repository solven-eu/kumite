package eu.solven.kumite.app;

import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

import eu.solven.kumite.account.fake_player.FakePlayer;
import eu.solven.kumite.account.fake_player.RandomPlayer;
import eu.solven.kumite.login.RefreshTokenWrapper;
import eu.solven.kumite.oauth2.authorizationserver.KumiteTokenService;
import eu.solven.kumite.tools.IUuidGenerator;
import eu.solven.kumite.tools.JdkUuidGenerator;
import lombok.Builder;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Value
@Builder
@Slf4j
public class KumiteWebclientServerProperties {
	public static final String KEY_PLAYER_CONTESTBASEURL = "kumite.player.contest-base-url";

	public static final String ENV_REFRESH_TOKEN = "kumite.player.refresh_token";
	public static final String PLACEHOLDER_GENERATEFAKEPLAYER = "GENERATE_FAKEUSER";
	public static final String PLACEHOLDER_GENERATERANDOMPLAYER = "GENERATE_RANDOMUSER";

	String baseUrl;
	String refreshToken;

	public static String loadRefreshToken(Environment env, IUuidGenerator uuidGenerator, String refreshToken) {
		if ("NEEDS_A_PROPER_VALUE".equals(refreshToken)) {
			throw new IllegalStateException(
					"Needs to define properly '%s'".formatted(KumiteWebclientServerProperties.ENV_REFRESH_TOKEN));
		} else if (KumiteWebclientServerProperties.PLACEHOLDER_GENERATEFAKEPLAYER.equals(refreshToken)) {
			if (!env.acceptsProfiles(Profiles.of(IKumiteSpringProfiles.P_FAKEUSER))) {
				throw new IllegalStateException(
						"Can not generate a refreshToken if not `%s`".formatted(IKumiteSpringProfiles.P_FAKEUSER));
			} else {
				log.info("Generating on-the-fly a fakeUser refreshToken");
			}
			KumiteTokenService kumiteTokenService = new KumiteTokenService(env, uuidGenerator);
			RefreshTokenWrapper wrappedRefreshToken =
					kumiteTokenService.wrapInJwtRefreshToken(FakePlayer.user(), FakePlayer.fakePlayers());
			refreshToken = wrappedRefreshToken.getRefreshToken();
		} else if (KumiteWebclientServerProperties.PLACEHOLDER_GENERATERANDOMPLAYER.equals(refreshToken)) {
			{
				log.info("Generating on-the-fly a fakeUser refreshToken");
			}
			KumiteTokenService kumiteTokenService = new KumiteTokenService(env, uuidGenerator);
			RefreshTokenWrapper wrappedRefreshToken =
					kumiteTokenService.wrapInJwtRefreshToken(RandomPlayer.user(), RandomPlayer.randomPlayers());
			refreshToken = wrappedRefreshToken.getRefreshToken();
		}
		return refreshToken;
	}

	public static KumiteWebclientServerProperties forTests(Environment env, int randomServerPort) {
		String refreshToken = loadRefreshToken(env,
				JdkUuidGenerator.INSTANCE,
				KumiteWebclientServerProperties.PLACEHOLDER_GENERATERANDOMPLAYER);

		// https://github.com/spring-projects/spring-boot/issues/5077
		String baseUrl = env.getRequiredProperty(KEY_PLAYER_CONTESTBASEURL)
				.replaceFirst("LocalServerPort", Integer.toString(randomServerPort));

		return KumiteWebclientServerProperties.builder().baseUrl(baseUrl).refreshToken(refreshToken).build();
	}
}
