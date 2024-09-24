package eu.solven.kumite.app.it.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.StatusAssertions;
import org.springframework.test.web.reactive.server.WebTestClient;

import eu.solven.kumite.account.fake_player.RandomPlayer;
import eu.solven.kumite.app.IKumiteSpringProfiles;
import eu.solven.kumite.app.webflux.KumiteWebExceptionHandler;
import eu.solven.kumite.app.webflux.api.AccessTokenHandler;
import eu.solven.kumite.app.webflux.api.GreetingHandler;
import eu.solven.kumite.app.webflux.api.KumiteLoginController;
import eu.solven.kumite.app.webflux.api.KumitePublicController;
import eu.solven.kumite.login.AccessTokenWrapper;
import eu.solven.kumite.oauth2.authorizationserver.KumiteTokenService;
import eu.solven.pepper.unittest.ILogDisabler;
import eu.solven.pepper.unittest.PepperTestHelper;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT enables logging-in a subset of APIs, especially the applicative (e.g. not login) APIs.
 * 
 * @author Benoit Lacelle
 *
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = KumiteServerSecurityApplication.class,
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({ IKumiteSpringProfiles.P_UNSAFE })
@Slf4j
// https://stackoverflow.com/questions/73881370/mocking-oauth2-client-with-webtestclient-for-servlet-applications-results-in-nul
@AutoConfigureWebTestClient
public class TestSecurity_WithJwtUser {

	@Autowired
	WebTestClient webTestClient;

	@Autowired
	KumiteTokenService tokenService;

	protected String generateAccessToken() {
		return tokenService.generateAccessToken(RandomPlayer.user(),
				Set.of(RandomPlayer.PLAYERID_1),
				Duration.ofMinutes(1),
				false);
	}

	protected String generateRefreshToken() {
		return tokenService
				.generateAccessToken(RandomPlayer.user(), Set.of(RandomPlayer.PLAYERID_1), Duration.ofMinutes(1), true);
	}

	@Test
	public void testApiPublic() {
		log.debug("About {}", GreetingHandler.class);
		log.debug("About {}", KumitePublicController.class);

		webTestClient

				.get()
				.uri("/api/v1/public")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + generateAccessToken())
				.accept(MediaType.APPLICATION_JSON)
				.exchange()

				.expectStatus()
				.isOk()
				.expectBody(String.class)
				.value(greeting -> assertThat(greeting).isEqualTo("This is a public endpoint"));
	}

	@Test
	public void testLogin() {
		log.debug("About {}", KumiteLoginController.class);

		webTestClient

				.get()
				.uri("/html/login")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + generateAccessToken())
				.accept(MediaType.TEXT_HTML)
				.exchange()

				.expectStatus()
				.isOk()
				.expectBody(String.class)
				.value(greeting -> assertThat(greeting).contains("<title>Kumite"));
	}

	@Test
	public void testLoginOptions() {
		log.debug("About {}", GreetingHandler.class);

		webTestClient

				.get()
				.uri("/api/login/v1/providers")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + generateAccessToken())
				.accept(MediaType.APPLICATION_JSON)
				.exchange()

				.expectStatus()
				.isOk()
				.expectBody(Map.class);
	}

	@Test
	public void testLoginUser() {
		log.debug("About {}", KumiteLoginController.class);

		try (ILogDisabler logDisabler = PepperTestHelper.disableLog(KumiteWebExceptionHandler.class)) {
			webTestClient

					.get()
					.uri("/api/login/v1/user")
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + generateAccessToken())
					.accept(MediaType.APPLICATION_JSON)
					.exchange()

					.expectStatus()
					// This routes requires OAuth2 authentication
					.isUnauthorized();
		}
	}

	@Test
	public void testLoginToken() {
		log.debug("About {}", KumiteLoginController.class);

		try (ILogDisabler logDisabler = PepperTestHelper.disableLog(KumiteWebExceptionHandler.class)) {
			webTestClient

					.get()
					.uri("/api/login/v1/oauth2/token")
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + generateAccessToken())
					.accept(MediaType.APPLICATION_JSON)
					.exchange()

					.expectStatus()
					// This routes requires OAuth2 authentication
					.isUnauthorized();
		}
	}

	@Test
	public void testLoginPage() {
		log.debug("About {}", KumiteLoginController.class);

		webTestClient

				.get()
				.uri("/api/login/v1/html")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + generateAccessToken())
				.accept(MediaType.APPLICATION_JSON)
				.exchange()

				.expectStatus()
				// This routes requires OAuth2 authentication
				.isFound()
				.expectHeader()
				.location("login");
	}

	@Test
	public void testApiPrivate() {
		log.debug("About {}", GreetingHandler.class);

		webTestClient.get()

				.uri("/api/v1/private")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + generateAccessToken())
				.accept(MediaType.APPLICATION_JSON)
				.exchange()

				.expectStatus()
				.isOk();
	}

	@Test
	public void testApiPrivate_unknownRoute() {
		log.debug("About {}", GreetingHandler.class);

		webTestClient.get()

				.uri("/api/private/unknown")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + generateAccessToken())
				.accept(MediaType.APPLICATION_JSON)
				.exchange()

				.expectStatus()
				.isNotFound();
	}

	@Test
	public void testApiPOSTWithCsrf() {
		log.debug("About {}", GreetingHandler.class);

		webTestClient
				// https://www.baeldung.com/spring-security-csrf
				.mutateWith(SecurityMockServerConfigurers.csrf())

				.post()
				.uri("/api/v1/hello")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + generateAccessToken())
				.bodyValue("{}")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()

				.expectStatus()
				.isOk();
	}

	@Test
	public void testApiPOSTWithoutCsrf() {
		log.debug("About {}", GreetingHandler.class);

		StatusAssertions expectStatus = webTestClient.post()
				.uri("/api/v1/hello")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + generateAccessToken())
				.bodyValue("{}")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus();

		expectStatus.isOk();
	}

	@Test
	public void testMakeRefreshToken() {
		log.debug("About {}", KumiteLoginController.class);

		try (ILogDisabler logDisabler = PepperTestHelper.disableLog(KumiteWebExceptionHandler.class)) {
			StatusAssertions expectStatus = webTestClient.get()
					.uri("/api/login/v1/oauth2/token?refresh_token=true")
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + generateAccessToken())
					.accept(MediaType.APPLICATION_JSON)
					.exchange()
					.expectStatus();

			// We need an oauth2 user, not a jwt user
			expectStatus.isUnauthorized().expectBody(Map.class).value(bodyAsMap -> {
				Assertions.assertThat(bodyAsMap).containsEntry("error_message", "No user").hasSize(1);
			});

		}
	}

	@Test
	public void testRefreshTokenToAccessToken() {
		log.debug("About {}", AccessTokenHandler.class);

		StatusAssertions expectStatus = webTestClient.get()
				.uri("/api/v1/oauth2/token?player_id=" + RandomPlayer.PLAYERID_1)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + generateRefreshToken())
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus();

		expectStatus.isOk().expectBody(AccessTokenWrapper.class).value(accessTokenHolder -> {
			Assertions.assertThat(accessTokenHolder.getAccessToken()).isNotEmpty();
		});
	}
}