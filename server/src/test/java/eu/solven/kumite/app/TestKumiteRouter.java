package eu.solven.kumite.app;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import eu.solven.kumite.contest.ContestMetadata;
import eu.solven.kumite.contest.ContestSearchHandler;
import eu.solven.kumite.game.GameMetadata;
import eu.solven.kumite.game.GameSearchHandler;
import eu.solven.kumite.greeting.Greeting;
import eu.solven.kumite.greeting.GreetingHandler;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@ExtendWith(SpringExtension.class)
// We create a `@SpringBootTest`, starting an actual server on a `RANDOM_PORT`
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class TestKumiteRouter {

	// Spring Boot will create a `WebTestClient` for you,
	// already configure and ready to issue requests against "localhost:RANDOM_PORT"
	@Autowired
	private WebTestClient webTestClient;

	@Test
	public void testHello() {
		log.debug("About {}", GreetingHandler.class);

		webTestClient
				// Create a GET request to test an endpoint
				.get()
				.uri("/hello")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				// and use the dedicated DSL to test assertions against the response
				.expectStatus()
				.isOk()
				.expectBody(Greeting.class)
				.value(greeting -> {
					assertThat(greeting.getMessage()).isEqualTo("Hello, Spring!");
				});
	}

	@Test
	public void testSearchGames() {
		log.debug("About {}", GameSearchHandler.class);

		webTestClient
				// Create a GET request to test an endpoint
				.get()
				.uri("/games")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				// and use the dedicated DSL to test assertions against the response
				.expectStatus()
				.isOk()
				.expectBodyList(GameMetadata.class)
				.value(greeting -> {
					// assertThat(greeting.getMessage()).isEqualTo("Hello, Spring!");
					assertThat(greeting).hasSize(1).element(0).matches(game -> {
						Assertions.assertThat(game.getTitle()).isEqualTo("Travelling Salesman Problem");

						Assertions.assertThat(game.getMinPlayers()).isEqualTo(1);
						Assertions.assertThat(game.getMaxPlayers()).isEqualTo(Integer.MAX_VALUE);

						return true;
					});
				});
	}

	@Test
	public void testSearchContests() {
		log.debug("About {}", ContestSearchHandler.class);

		webTestClient
				// Create a GET request to test an endpoint
				.get()
				.uri("/games")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				// and use the dedicated DSL to test assertions against the response
				.expectStatus()
				.isOk()
				.expectBodyList(ContestMetadata.class)
				.value(contests -> {
					// assertThat(greeting.getMessage()).isEqualTo("Hello, Spring!");
					assertThat(contests).hasSize(1).element(0).matches(contest -> {
						@NonNull
						GameMetadata game = contest.getGameMetadata();
						Assertions.assertThat(game.getTitle()).isEqualTo("Travelling Salesman Problem");

						Assertions.assertThat(game.getMinPlayers()).isEqualTo(1);
						Assertions.assertThat(game.getMaxPlayers()).isEqualTo(Integer.MAX_VALUE);

						return true;
					});
				});
	}
}