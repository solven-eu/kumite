package eu.solven.kumite.app.it.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.List;
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

import eu.solven.kumite.account.fake_player.FakePlayerTokens;
import eu.solven.kumite.app.IKumiteSpringProfiles;
import eu.solven.kumite.app.controllers.KumiteLoginController;
import eu.solven.kumite.app.controllers.KumitePublicController;
import eu.solven.kumite.app.greeting.GreetingHandler;
import eu.solven.kumite.app.webflux.AccessTokenHandler;
import eu.solven.kumite.app.webflux.KumiteExceptionRoutingWebFilter;
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
		return tokenService.generateAccessToken(FakePlayerTokens.fakeUser(),
				Set.of(FakePlayerTokens.FAKE_PLAYER_ID1),
				Duration.ofMinutes(1),
				false);
	}

	protected String generateRefreshToken() {
		return tokenService.generateAccessToken(FakePlayerTokens.fakeUser(),
				Set.of(FakePlayerTokens.FAKE_PLAYER_ID1),
				Duration.ofMinutes(1),
				true);
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
				.expectBody(Map.class)
				.value(greeting -> {
					Map<String, ?> asMap = (Map<String, ?>) greeting.get("map");
					assertThat(asMap).hasSize(2).containsOnlyKeys("github", "google");

					Assertions.assertThat((Map) asMap.get("github"))
							.containsEntry("login_url", "/oauth2/authorization/github");

					List<Map<String, ?>> asList = (List<Map<String, ?>>) greeting.get("list");
					assertThat(asList).hasSize(2).anySatisfy(m -> {
						Assertions.assertThat((Map) m)
								.containsEntry("login_url", "/oauth2/authorization/github")
								.hasSize(2);
					}).anySatisfy(m -> {
						Assertions.assertThat((Map) m).containsEntry("login_url", "/oauth2/authorization/google");
					});
				});
	}

	@Test
	public void testLoginUser() {
		log.debug("About {}", KumiteLoginController.class);

		try (ILogDisabler logDisabler = PepperTestHelper.disableLog(KumiteExceptionRoutingWebFilter.class)) {
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

		try (ILogDisabler logDisabler = PepperTestHelper.disableLog(KumiteExceptionRoutingWebFilter.class)) {
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

		try (ILogDisabler logDisabler = PepperTestHelper.disableLog(KumiteExceptionRoutingWebFilter.class)) {
			StatusAssertions expectStatus = webTestClient.get()
					.uri("/api/login/v1/oauth2/token?refresh_token=true")
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + generateAccessToken())
					.accept(MediaType.APPLICATION_JSON)
					.exchange()
					.expectStatus();

			// We need an oauth2 user, not a jwt user
			expectStatus.isUnauthorized();
		}
	}

	@Test
	public void testRefreshTokenToAccessToken() {
		log.debug("About {}", AccessTokenHandler.class);

		StatusAssertions expectStatus = webTestClient.get()
				.uri("/api/v1/oauth2/token?player_id=11111111-1111-1111-1111-111111111111")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + generateRefreshToken())
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus();

		expectStatus.isOk().expectBody(AccessTokenWrapper.class).value(accessTokenHolder -> {
			Assertions.assertThat(accessTokenHolder.getAccessToken()).isNotEmpty();
		});
	}
}