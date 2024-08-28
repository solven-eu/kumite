package eu.solven.kumite.app.it.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import eu.solven.kumite.greeting.GreetingHandler;
import lombok.extern.slf4j.Slf4j;

@ExtendWith(SpringExtension.class)
// We create a `@SpringBootTest`, starting an actual server on a `RANDOM_PORT`
@SpringBootTest(classes = KumiteServerSecurityApplication.class,
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class TestSecurity {

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
				.uri("/login/providers")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				// and use the dedicated DSL to test assertions against the response
				.expectStatus()
				.isOk()
				.expectBody(Map.class)
				.value(greeting -> {
					assertThat(greeting).hasSize(2).containsOnlyKeys("github", "google");

					Assertions.assertThat((Map) greeting.get("github"))
							.containsEntry("login_url", "/oauth2/authorization/github");
				});
	}

	@Test
	public void testApiPrivate() {
		log.debug("About {}", GreetingHandler.class);

		webTestClient
				// Create a GET request to test an endpoint
				.get()
				.uri("/api/private")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				// and use the dedicated DSL to test assertions against the response
				.expectStatus()
				.isUnauthorized()
		// .expectHeader()
		// .contentType(MediaType.APPLICATION_JSON)
		;
	}
}