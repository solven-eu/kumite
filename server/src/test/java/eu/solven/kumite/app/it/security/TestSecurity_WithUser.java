package eu.solven.kumite.app.it.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.StatusAssertions;
import org.springframework.test.web.reactive.server.WebTestClient;

import eu.solven.kumite.account.login.SocialWebFluxSecurity;
import eu.solven.kumite.app.controllers.KumiteLoginController;
import eu.solven.kumite.app.greeting.GreetingHandler;
import lombok.extern.slf4j.Slf4j;

@ExtendWith(SpringExtension.class)
// We create a `@SpringBootTest`, starting an actual server on a `RANDOM_PORT`
@SpringBootTest(classes = KumiteServerSecurityApplication.class,
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
// https://stackoverflow.com/questions/73881370/mocking-oauth2-client-with-webtestclient-for-servlet-applications-results-in-nul
@AutoConfigureWebTestClient
@WithMockUser
public class TestSecurity_WithUser {

	// Spring Boot will create a `WebTestClient` for you,
	// already configure and ready to issue requests against "localhost:RANDOM_PORT"
	@Autowired
	private WebTestClient webTestClient;

	@Test
	public void testApiPublic() {
		log.debug("About {}", GreetingHandler.class);

		webTestClient
				// Create a GET request to test an endpoint
				.get()
				.uri("/api/public")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				// and use the dedicated DSL to test assertions against the response
				.expectStatus()
				.isOk()
				.expectBody(String.class)
				.value(greeting -> {
					assertThat(greeting).isEqualTo("This is a public endpoint");
				});
	}

	@Test
	public void testLoginOptions() {
		log.debug("About {}", GreetingHandler.class);

		webTestClient
				// Create a GET request to test an endpoint
				.get()
				.uri("/api/login/v1/providers")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				// and use the dedicated DSL to test assertions against the response
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

		webTestClient
				// Create a GET request to test an endpoint
				.get()
				.uri("/api/login/v1/user")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				// and use the dedicated DSL to test assertions against the response
				.expectStatus()
				.isOk()
				.expectBody(String.class)
				.value(greeting -> {
					Assertions.assertThat(greeting).isEqualTo("ss");
				});
	}

	@Test
	public void testLoginToken() {
		log.debug("About {}", KumiteLoginController.class);

		webTestClient
				// Create a GET request to test an endpoint
				.get()
				.uri("/api/login/v1/token")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				// and use the dedicated DSL to test assertions against the response
				.expectStatus()
				.isOk()
				.expectBody(String.class)
				.value(greeting -> {
					Assertions.assertThat(greeting).isEqualTo("ss");
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