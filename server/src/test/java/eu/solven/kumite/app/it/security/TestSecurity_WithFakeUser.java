package eu.solven.kumite.app.it.security;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import eu.solven.kumite.account.fake_player.FakePlayer;
import eu.solven.kumite.app.IKumiteSpringProfiles;
import eu.solven.kumite.app.KumiteJackson;
import eu.solven.kumite.app.webflux.api.KumiteLoginController;
import eu.solven.kumite.login.AccessTokenWrapper;
import lombok.extern.slf4j.Slf4j;

/**
 * OAuth2 enables logging-in a subset of APIs, especially the login APIs.
 * 
 * @author Benoit Lacelle
 *
 */
@ExtendWith(SpringExtension.class)
@ActiveProfiles({ IKumiteSpringProfiles.P_UNSAFE, IKumiteSpringProfiles.P_FAKEUSER })
@Slf4j
public class TestSecurity_WithFakeUser extends TestSecurity_WithOAuth2User {

	@Test
	@Override
	public void testLoginAccessToken() {
		log.debug("About {}", KumiteLoginController.class);

		webTestClient

				.get()
				.uri("/api/login/v1/oauth2/token")
				.accept(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION,
						"Basic " + HttpHeaders.encodeBasicAuth(FakePlayer.ACCOUNT_ID.toString(),
								"no_password",
								StandardCharsets.UTF_8))
				.exchange()

				.expectStatus()
				.isOk()
				.expectBody(AccessTokenWrapper.class)
				.value(token -> {
					Map asMap = KumiteJackson.objectMapper().convertValue(token, Map.class);

					Assertions.assertThat(asMap)
							.containsKey("access_token")
							.containsEntry("token_type", "Bearer")
							.containsEntry("player_id", FakePlayer.PLAYER_ID1.toString())
							.containsEntry("expires_in", 3600L)
							.hasSize(4);
				});
	}

	@Test
	public void testLoginAccessToken_invalidUser() {
		log.debug("About {}", KumiteLoginController.class);

		webTestClient

				.get()
				.uri("/api/login/v1/oauth2/token")
				.accept(MediaType.APPLICATION_JSON)
				.header(HttpHeaders.AUTHORIZATION,
						"Basic " + HttpHeaders
								.encodeBasicAuth("someUnknownUser", "no_password", StandardCharsets.UTF_8))
				.exchange()

				.expectStatus()
				.isUnauthorized();
	}
}