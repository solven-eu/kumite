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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.OAuth2LoginMutator;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.StatusAssertions;
import org.springframework.test.web.reactive.server.WebTestClient;

import eu.solven.kumite.account.KumiteUser;
import eu.solven.kumite.account.KumiteUserRaw;
import eu.solven.kumite.account.login.KumiteOAuth2UserService;
import eu.solven.kumite.account.login.SocialWebFluxSecurity;
import eu.solven.kumite.app.IKumiteSpringProfiles;
import eu.solven.kumite.app.controllers.KumiteLoginController;
import eu.solven.kumite.app.greeting.GreetingHandler;
import eu.solven.kumite.scenario.TestTSPLifecycle;
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
@ActiveProfiles({ IKumiteSpringProfiles.P_UNSAFE_SERVER, })
@Slf4j
// https://stackoverflow.com/questions/73881370/mocking-oauth2-client-with-webtestclient-for-servlet-applications-results-in-nul
@AutoConfigureWebTestClient
@WithMockUser
public class TestSecurity_WithOAuth2User {

	// Spring Boot will create a `WebTestClient` for you,
	// already configure and ready to issue requests against "localhost:RANDOM_PORT"
	@Autowired
	private WebTestClient webTestClient;

	@Autowired
	KumiteOAuth2UserService oauth2UserService;

	@Test
	public void testApiPublic() {
		log.debug("About {}", GreetingHandler.class);

		webTestClient

				.get()
				.uri("/api/public")
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

		// Beware `.mutateWith(oauth2Login)` skips KumiteOAuth2UserService, hence automated registration on first OAuth2
		// login
		OAuth2LoginMutator oauth2Login;
		{
			KumiteUserRaw userRaw = TestTSPLifecycle.userRaw();
			oauth2Login = SecurityMockServerConfigurers.mockOAuth2Login().attributes(attributes -> {
				attributes.put("id", userRaw.getRawRaw().getSub());
				attributes.put("providerId", userRaw.getRawRaw().getProviderId());
			});
			oauth2UserService.onKumiteUserRaw(userRaw);
		}

		webTestClient

				.mutateWith(oauth2Login)

				.get()
				.uri("/api/login/v1/user")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()

				.expectStatus()
				.isOk()
				.expectBody(Map.class)
				.value(greeting -> {
					Assertions.assertThat(greeting).containsKeys("accountId", "raw").hasSize(4);
				});
	}

	@Test
	public void testLoginToken() {
		log.debug("About {}", KumiteLoginController.class);

		KumiteUser kumiteUser;

		// Beware `.mutateWith(oauth2Login)` skips KumiteOAuth2UserService, hence automated registration on first OAuth2
		// login
		OAuth2LoginMutator oauth2Login;
		{
			KumiteUserRaw userRaw = TestTSPLifecycle.userRaw();
			oauth2Login = SecurityMockServerConfigurers.mockOAuth2Login().attributes(attributes -> {
				attributes.put("id", userRaw.getRawRaw().getSub());
				attributes.put("providerId", userRaw.getRawRaw().getProviderId());
			});
			kumiteUser = oauth2UserService.onKumiteUserRaw(userRaw);
		}

		webTestClient

				.mutateWith(oauth2Login)

				.get()
				.uri("/api/login/v1/token")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()

				.expectStatus()
				.isOk()
				.expectBody(Map.class)
				.value(tokens -> {
					Assertions.assertThat(tokens)
							.containsKey("access_token")
							.containsEntry("token_type", "Bearer")
							.containsEntry("player_id", kumiteUser.getPlayerId().toString())
							.containsEntry("expires_in", 3600)
							.hasSize(4);
				});
	}

	@Test
	public void testApiPrivate() {
		log.debug("About {}", GreetingHandler.class);

		webTestClient.get().uri("/api/private").accept(MediaType.APPLICATION_JSON).exchange().expectStatus().isOk();
	}

	@Test
	public void testApiPrivate_unknownRoute() {
		log.debug("About {}", GreetingHandler.class);

		webTestClient.get()
				.uri("/api/private/unknown")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus()
				.isNotFound();
	}

	@Test
	public void testApiPrivatePostMoveWithCsrf() {
		log.debug("About {}", GreetingHandler.class);

		webTestClient
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

	@Test
	public void testApiPrivatePostMoveWithoutCsrf() {
		log.debug("About {}", GreetingHandler.class);

		StatusAssertions expectStatus = webTestClient.post()
				.uri("/api/board/move?contest_id=7ffcb8e6-bf71-4817-9f72-077c22172643&player_id=11111111-1111-1111-1111-111111111111")
				.bodyValue("{}")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus();

		if (SocialWebFluxSecurity.DISABLE_CSRF_CORS) {
			expectStatus.isNotFound();
		} else {
			expectStatus.isForbidden();
		}
	}
}