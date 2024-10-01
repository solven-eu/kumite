package eu.solven.kumite.app.it.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.OAuth2LoginMutator;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import eu.solven.kumite.account.internal.KumiteUser;
import eu.solven.kumite.account.internal.KumiteUserPreRegister;
import eu.solven.kumite.account.internal.KumiteUserRaw;
import eu.solven.kumite.account.login.IKumiteTestConstants;
import eu.solven.kumite.app.IKumiteSpringProfiles;
import eu.solven.kumite.app.KumiteJackson;
import eu.solven.kumite.app.webflux.KumiteWebExceptionHandler;
import eu.solven.kumite.app.webflux.api.AccessTokenHandler;
import eu.solven.kumite.app.webflux.api.GreetingHandler;
import eu.solven.kumite.app.webflux.api.KumiteLoginController;
import eu.solven.kumite.app.webflux.api.KumitePublicController;
import eu.solven.kumite.login.AccessTokenWrapper;
import eu.solven.kumite.login.RefreshTokenWrapper;
import eu.solven.kumite.player.PlayerMovesHandler;
import eu.solven.kumite.security.oauth2.KumiteOAuth2UserService;
import eu.solven.pepper.unittest.ILogDisabler;
import eu.solven.pepper.unittest.PepperTestHelper;
import lombok.extern.slf4j.Slf4j;

/**
 * OAuth2 enables logging-in a subset of APIs, especially the login APIs.
 * 
 * @author Benoit Lacelle
 *
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = KumiteServerSecurityApplication.class,
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({ IKumiteSpringProfiles.P_UNSAFE, })
@Slf4j
// https://stackoverflow.com/questions/73881370/mocking-oauth2-client-with-webtestclient-for-servlet-applications-results-in-nul
// https://stackoverflow.com/questions/56784289/autoconfigurewebtestclienttimeout-600000-has-no-effect
@AutoConfigureWebTestClient(timeout = "PT10M")
// @WithMockUser
public class TestSecurity_WithOAuth2User {

	// Spring Boot will create a `WebTestClient` for you,
	// already configure and ready to issue requests against "localhost:RANDOM_PORT"
	@Autowired
	WebTestClient webTestClient;

	@Autowired
	KumiteOAuth2UserService oauth2UserService;

	private OAuth2LoginMutator prepareLogin() {
		// Beware `.mutateWith(oauth2Login)` skips KumiteOAuth2UserService, hence automated registration on first OAuth2
		// login
		OAuth2LoginMutator oauth2Login;
		{
			KumiteUserPreRegister userPreRegister = IKumiteTestConstants.userPreRegister();
			oauth2Login = SecurityMockServerConfigurers.mockOAuth2Login().attributes(attributes -> {
				attributes.put("id", userPreRegister.getRawRaw().getSub());
				attributes.put("providerId", userPreRegister.getRawRaw().getProviderId());
			});
			oauth2UserService.onKumiteUserRaw(userPreRegister);
		}
		return oauth2Login;
	}

	WebTestClient getWebTestClient() {
		return webTestClient.mutateWith(prepareLogin());
	}

	@Test
	public void testApiPublic() {
		log.debug("About {}", GreetingHandler.class);
		log.debug("About {}", KumitePublicController.class);

		getWebTestClient()

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
	public void testLogin() {
		log.debug("About {}", KumiteLoginController.class);

		getWebTestClient()

				.get()
				.uri("/html/login")
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

		getWebTestClient()

				.get()
				.uri("/api/login/v1/providers")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()

				.expectStatus()
				.isOk()
				.expectBody(Map.class)
				.value(loginOptions -> {
					onLoginOptions(loginOptions);
				});
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void onLoginOptions(Map loginOptions) {
		Map<String, ?> asMap = (Map<String, ?>) loginOptions.get("map");
		assertThat(asMap).hasSize(2).containsKeys("github", "google");

		Assertions.assertThat((Map) asMap.get("github")).containsEntry("login_url", "/oauth2/authorization/github");

		List<Map<String, ?>> asList = (List<Map<String, ?>>) loginOptions.get("list");
		assertThat(asList).hasSize(2).anySatisfy(m -> {
			Assertions.assertThat((Map) m).containsEntry("login_url", "/oauth2/authorization/github").hasSize(4);
		}).anySatisfy(m -> {
			Assertions.assertThat((Map) m).containsEntry("login_url", "/oauth2/authorization/google");
		});
	}

	@Test
	public void testLoginUser() {
		log.debug("About {}", KumiteLoginController.class);

		getWebTestClient()

				.get()
				.uri("/api/login/v1/user")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()

				.expectStatus()
				.isOk()
				.expectBody(KumiteUserRaw.class)
				.value(user -> {
					Assertions.assertThat(user.getDetails()).isNotNull();
				});
	}

	@Test
	public void testLoginUser_update() {
		log.debug("About {}", KumiteLoginController.class);

		getWebTestClient()

				// https://www.baeldung.com/spring-security-csrf
				.mutateWith(SecurityMockServerConfigurers.csrf())

				.post()
				.uri("/api/login/v1/user")
				.accept(MediaType.APPLICATION_JSON)
				.bodyValue(Map.of("countryCode", "someCountryCode"))
				.exchange()

				.expectStatus()
				.isOk()
				.expectBody(KumiteUserRaw.class)
				.value(user -> {
					Assertions.assertThat(user.getDetails().getCountryCode()).isEqualTo("someCountryCode");
				});
	}

	@Test
	public void testLoginJson() {
		log.debug("About {}", GreetingHandler.class);

		getWebTestClient()

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
					Assertions.assertThat(body).containsEntry("login", 200).hasSize(1);
				});
	}

	@Test
	public void testLogout() {
		log.debug("About {}", KumiteLoginController.class);

		// SPA does a first call triggering the Logout: it must returns a 2XX response, as Fetch can not intercept 3XX.
		getWebTestClient()

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
		// This second call is not authenticated
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
	public void testLoginAccessToken() {
		log.debug("About {}", KumiteLoginController.class);

		KumiteUser kumiteUser;

		// Beware `.mutateWith(oauth2Login)` skips KumiteOAuth2UserService, hence automated registration on first OAuth2
		// login
		OAuth2LoginMutator oauth2Login;
		{
			KumiteUserPreRegister userPreRegister = IKumiteTestConstants.userPreRegister();
			oauth2Login = SecurityMockServerConfigurers.mockOAuth2Login().attributes(attributes -> {
				attributes.put("id", userPreRegister.getRawRaw().getSub());
				attributes.put("providerId", userPreRegister.getRawRaw().getProviderId());
			});
			kumiteUser = oauth2UserService.onKumiteUserRaw(userPreRegister);
		}

		webTestClient

				.mutateWith(oauth2Login)

				.get()
				.uri("/api/login/v1/oauth2/token")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()

				.expectStatus()
				.isOk()
				.expectBody(AccessTokenWrapper.class)
				.value(token -> {
					Map asMap = KumiteJackson.objectMapper().convertValue(token, Map.class);

					Assertions.assertThat(asMap)
							.containsKey("access_token")
							.containsEntry("token_type", "Bearer")
							.containsEntry("player_id", kumiteUser.getPlayerId().toString())
							.containsEntry("expires_in", 3600L)
							.hasSize(4);
				});
	}

	@Test
	public void testLoginPage() {
		log.debug("About {}", KumiteLoginController.class);

		getWebTestClient()

				.get()
				.uri("/api/login/v1/html")
				// .accept(MediaType.APPLICATION_JSON)
				.exchange()

				.expectStatus()
				.isFound()
				.expectHeader()
				.location("login?success");
	}

	@Test
	public void testApiPrivate() {
		log.debug("About {}", GreetingHandler.class);

		getWebTestClient()

				.get()
				.uri("/api/v1/private")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()

				.expectStatus()
				.isOk();
	}

	@Test
	public void testApiPrivate_unknownRoute() {
		log.debug("About {}", GreetingHandler.class);

		getWebTestClient()

				.get()
				.uri("/api/v1/private/unknown")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus()
				.isNotFound();
	}

	// CSRF is deactivated on JWT-API
	@Test
	public void testApiPrivatePostMoveWithCsrf() {
		log.debug("About {}", PlayerMovesHandler.class);

		getWebTestClient()

				// https://www.baeldung.com/spring-security-csrf
				.mutateWith(SecurityMockServerConfigurers.csrf())

				.post()
				.uri("/api/board/move?contest_id=7ffcb8e6-bf71-4817-9f72-077c22172643&player_id=11111111-1111-1111-1111-111111111111")
				.bodyValue("{}")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()

				.expectStatus()
				.isNotFound();
	}

	// CSRF is deactivated on JWT-API
	@Test
	public void testApiPrivatePostMoveWithoutCsrf() {
		log.debug("About {}", PlayerMovesHandler.class);

		getWebTestClient()

				.post()
				.uri("/api/board/move?contest_id=7ffcb8e6-bf71-4817-9f72-077c22172643&player_id=11111111-1111-1111-1111-111111111111")
				.bodyValue("{}")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()

				.expectStatus()
				.isNotFound();
	}

	@Test
	public void testMakeRefreshToken() {
		log.debug("About {}", KumiteLoginController.class);

		getWebTestClient()

				.get()
				.uri("/api/login/v1/oauth2/token?refresh_token=true")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()

				.expectStatus()
				.isOk()
				.expectBody(RefreshTokenWrapper.class)
				.value(accessTokenHolder -> {
					Assertions.assertThat(accessTokenHolder.getRefreshToken()).isNotEmpty();
				});
	}

	@Test
	public void testRefreshTokenToAccessToken() {
		log.debug("About {}", AccessTokenHandler.class);

		try (ILogDisabler logDisabler = PepperTestHelper.disableLog(KumiteWebExceptionHandler.class)) {
			getWebTestClient()

					.get()
					.uri("/api/v1/oauth2/token?player_id=11111111-1111-1111-1111-111111111111")
					.accept(MediaType.APPLICATION_JSON)
					.exchange()

					.expectStatus()
					.isUnauthorized();
		}
	}
}