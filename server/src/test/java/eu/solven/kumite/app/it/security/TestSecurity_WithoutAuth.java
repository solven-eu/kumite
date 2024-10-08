package eu.solven.kumite.app.it.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.StatusAssertions;
import org.springframework.test.web.reactive.server.WebTestClient;

import eu.solven.kumite.account.fake_player.FakePlayer;
import eu.solven.kumite.app.IKumiteSpringProfiles;
import eu.solven.kumite.app.webflux.KumiteWebExceptionHandler;
import eu.solven.kumite.app.webflux.api.AccessTokenHandler;
import eu.solven.kumite.app.webflux.api.GreetingHandler;
import eu.solven.kumite.app.webflux.api.KumiteLoginController;
import eu.solven.kumite.app.webflux.api.KumitePublicController;
import eu.solven.pepper.unittest.ILogDisabler;
import eu.solven.pepper.unittest.PepperTestHelper;
import lombok.extern.slf4j.Slf4j;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = KumiteServerSecurityApplication.class,
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
// https://stackoverflow.com/questions/73881370/mocking-oauth2-client-with-webtestclient-for-servlet-applications-results-in-nul
@ActiveProfiles({ IKumiteSpringProfiles.P_UNSAFE })
@AutoConfigureWebTestClient(timeout = "P1D")
public class TestSecurity_WithoutAuth {

	@Autowired
	private WebTestClient webTestClient;

	@Test
	public void testApiPublic() {
		log.debug("About {}", GreetingHandler.class);
		log.debug("About {}", KumitePublicController.class);

		webTestClient

				.get()
				.uri("/api/v1/public")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()

				.expectStatus()
				.isOk()
				.expectBody(String.class)
				.value(greeting -> assertThat(greeting).isEqualTo("This is a public endpoint"));
	}

	@Test
	public void testApiFavicon() {
		log.debug("About {}", GreetingHandler.class);

		webTestClient

				.get()
				.uri("/favicon.ico")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()

				.expectStatus()
				.isOk()
				.expectBody(byte[].class)
				.value(byteArray -> assertThat(byteArray).hasSize(15_406));
	}

	@Test
	public void testApiSpaRoute() {
		log.debug("About {}", GreetingHandler.class);

		webTestClient

				.get()
				.uri("/html/some/route")
				.accept(MediaType.TEXT_HTML)
				.exchange()

				.expectStatus()
				.isOk()
				.expectBody(String.class)
				.value(indexHtml -> assertThat(indexHtml).contains("<title>Kumite"));
	}

	@Test
	public void testLogin() {
		log.debug("About {}", KumiteLoginController.class);

		webTestClient

				.get()
				.uri("/html/login")
				.accept(MediaType.TEXT_HTML)
				.exchange()

				.expectStatus()
				.isOk()
				.expectBody(String.class)
				.value(indexHtml -> assertThat(indexHtml).contains("<title>Kumite"));
	}

	@Test
	public void testLoginOptions() {
		log.debug("About {}", GreetingHandler.class);

		webTestClient

				.get()
				.uri("/api/login/v1/providers")
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
					assertThat(asList).hasSize(2)
							.element(0)
							.asInstanceOf(InstanceOfAssertFactories.MAP)
							.containsEntry("login_url", "/oauth2/authorization/github");
				});
	}

	@Test
	public void testLoginUser() {
		log.debug("About {}", KumiteLoginController.class);

		try (ILogDisabler logDisabler = PepperTestHelper.disableLog(KumiteWebExceptionHandler.class)) {
			webTestClient

					.get()
					.uri("/api/login/v1/user")
					.accept(MediaType.APPLICATION_JSON)
					.exchange()

					// By default, oauth2 returns a 302 if not logged-in
					// Though we prefer to return a nice API answer
					.expectStatus()
					.isUnauthorized();
		}
	}

	@Test
	public void testLoginJson() {
		log.debug("About {}", KumiteLoginController.class);

		webTestClient

				.get()
				.uri("/api/login/v1/json")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()

				// By default, oauth2 returns a 302 if not logged-in
				// Though we prefer to return a nice API answer
				.expectStatus()
				.isOk()
				.expectBody(Map.class)
				.value(body -> {
					Assertions.assertThat(body).containsEntry("login", 401).hasSize(1);
				});
	}

	@Test
	public void testLogout() {
		log.debug("About {}", KumiteLoginController.class);

		// SPA does a first call triggering the Logout: it must returns a 2XX response, as Fetch can not intercept 3XX.
		webTestClient

				// https://www.baeldung.com/spring-security-csrf
				.mutateWith(SecurityMockServerConfigurers.csrf())

				.post()
				.uri("/logout")
				// .accept(MediaType.APPLICATION_JSON)
				.exchange()

				.expectStatus()
				.isFound()
				.expectHeader()
				.location("/api/login/v1/logout");

		// SPA will then redirect the browser to URL provided in 2XX
		webTestClient

				.get()
				.uri("/api/login/v1/logout")
				// .accept(MediaType.APPLICATION_JSON)
				.exchange()

				.expectStatus()
				.isOk()
				.expectBody(Map.class)
				.value(bodyAsMap -> {
					// Ensure the csrfToken is not in the body, as it would make it easier to leak
					Assertions.assertThat(bodyAsMap).containsEntry("Location", "/html/login?logout").hasSize(1);
				});
	}

	@Test
	public void testGetCsrf() {
		log.debug("About {}", KumiteLoginController.class);

		AtomicReference<String> refCsrfToken = new AtomicReference<>();

		webTestClient

				.get()
				.uri("/api/login/v1/csrf")
				.exchange()

				.expectStatus()
				.isOk()

				.expectHeader()
				.value("X-CSRF-TOKEN", csrfToken -> {
					Assertions.assertThat(csrfToken).hasSizeGreaterThan(16);

					refCsrfToken.set(csrfToken);
				})

				.expectBody(Map.class)
				.value(bodyAsMap -> {
					// Ensure the csrfToken is not in the body, as it would make it easier to leak
					Assertions.assertThat(bodyAsMap).containsEntry("header", "X-CSRF-TOKEN").hasSize(1);
				});

		// TODO Could we check the csrfToken by doing a `POST /logout` with it? We would also need the Cookie SESSION
		// for this scenario to work
	}

	@Test
	public void testLoginToken() {
		log.debug("About {}", GreetingHandler.class);

		try (ILogDisabler logDisabler = PepperTestHelper.disableLog(KumiteWebExceptionHandler.class)) {
			webTestClient

					.get()
					.uri("/api/login/v1/oauth2/token")
					.accept(MediaType.APPLICATION_JSON)
					.exchange()

					// By default, oauth2 returns a 302 if not logged-in
					.expectStatus()
					.isUnauthorized();
		}
	}

	@Test
	public void testLoginPage() {
		log.debug("About {}", KumiteLoginController.class);

		webTestClient

				.get()
				.uri("/api/login/v1/html")
				// .accept(MediaType.APPLICATION_JSON)
				.exchange()

				.expectStatus()
				.isFound()
				.expectHeader()
				.location("login");
	}

	@Test
	public void testLoginUnknown() {
		log.debug("About {}", GreetingHandler.class);

		webTestClient

				.get()
				.uri("/api/login/v1/unknown")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()

				// By default, oauth2 returns a 302 if not logged-in
				.expectStatus()
				.isFound()
				.expectHeader()
				.location("/login");
	}

	@Test
	public void testApiPrivate() {
		log.debug("About {}", GreetingHandler.class);

		try (ILogDisabler logDisabler = PepperTestHelper.disableLog(KumiteWebExceptionHandler.class)) {
			webTestClient

					.get()
					.uri("/api/private")
					.accept(MediaType.APPLICATION_JSON)
					.exchange()

					.expectStatus()
					.isUnauthorized();
		}
	}

	@Test
	public void testApiPrivate_unknownRoute() {
		log.debug("About {}", GreetingHandler.class);

		try (ILogDisabler logDisabler = PepperTestHelper.disableLog(KumiteWebExceptionHandler.class)) {
			webTestClient

					.get()
					.uri("/api/private/unknown")
					.accept(MediaType.APPLICATION_JSON)
					.exchange()

					.expectStatus()
					.isUnauthorized();
		}
	}

	// TODO Change the route to make sure CSRF and CORS are OK on the first securityFilterChain
	@Test
	public void testApiPOSTWithCsrf() {
		log.debug("About {}", KumitePublicController.class);

		try (ILogDisabler logDisabler = PepperTestHelper.disableLog(KumiteWebExceptionHandler.class)) {
			webTestClient
					// https://www.baeldung.com/spring-security-csrf
					.mutateWith(SecurityMockServerConfigurers.csrf())

					.post()
					.uri("/api/v1/hello")
					.bodyValue("{}")
					.accept(MediaType.APPLICATION_JSON)
					.exchange()

					.expectStatus()
					.isUnauthorized();
		}
	}

	// TODO Change the route to make sure CSRF and CORS are OK on the first securityFilterChain
	@Test
	public void testApiPOSTWithoutCsrf() {
		log.debug("About {}", KumitePublicController.class);

		try (ILogDisabler logDisabler = PepperTestHelper.disableLog(KumiteWebExceptionHandler.class)) {
			StatusAssertions expectStatus = webTestClient.post()
					.uri("/api/v1/hello")
					.bodyValue("{}")
					.accept(MediaType.APPLICATION_JSON)
					.exchange()
					.expectStatus();

			expectStatus.isUnauthorized();
		}
	}

	@Test
	public void testMakeRefreshToken() {
		log.debug("About {}", KumiteLoginController.class);

		try (ILogDisabler logDisabler = PepperTestHelper.disableLog(KumiteWebExceptionHandler.class)) {
			StatusAssertions expectStatus = webTestClient.get()
					.uri("/api/login/v1/oauth2/token?refresh_token=true")
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

		try (ILogDisabler logDisabler = PepperTestHelper.disableLog(KumiteWebExceptionHandler.class)) {
			StatusAssertions expectStatus = webTestClient.get()
					.uri("/api/v1/oauth2/token?player_id=" + FakePlayer.fakePlayerId(0))
					.accept(MediaType.APPLICATION_JSON)
					.exchange()
					.expectStatus();

			expectStatus.isUnauthorized();
		}
	}

	@Test
	public void testOpenApi() {
		log.debug("About {}", AccessTokenHandler.class);

		StatusAssertions expectStatus =
				webTestClient.get().uri("/v3/api-docs").accept(MediaType.APPLICATION_JSON).exchange().expectStatus();

		expectStatus.isOk();
	}
}