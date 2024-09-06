package eu.solven.kumite.app.it;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import eu.solven.kumite.app.IKumiteSpringProfiles;
import eu.solven.kumite.app.KumiteServerApplication;
import eu.solven.kumite.app.greeting.Greeting;
import eu.solven.kumite.app.greeting.GreetingHandler;
import eu.solven.kumite.contest.ContestMetadataRaw;
import eu.solven.kumite.contest.ContestSearchHandler;
import eu.solven.kumite.game.GameMetadata;
import eu.solven.kumite.game.GameSearchHandler;
import eu.solven.kumite.game.optimization.tsp.TravellingSalesmanProblem;
import lombok.extern.slf4j.Slf4j;

@ExtendWith(SpringExtension.class)
// We create a `@SpringBootTest`, starting an actual server on a `RANDOM_PORT`
@SpringBootTest(classes = KumiteServerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({ IKumiteSpringProfiles.P_DEFAULT,
		IKumiteSpringProfiles.P_INJECT_DEFAULT_GAMES,
		IKumiteSpringProfiles.P_DEFAULT_FAKE_PLAYER, })
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

				.get()
				.uri("/api/hello")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()

				.expectStatus()
				.isOk()
				.expectBody(Greeting.class)
				.value(greeting -> {
					Assertions.assertThat(greeting.getMessage()).isEqualTo("Hello, Spring!");
				});
	}

	@Test
	public void testSearchGames() {
		log.debug("About {}", GameSearchHandler.class);

		webTestClient

				.get()
				.uri("/api/games")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()

				.expectStatus()
				.isOk()
				.expectBodyList(GameMetadata.class)
				.value(games -> {
					Assertions.assertThat(games)
							.hasSizeGreaterThanOrEqualTo(2)

							.anySatisfy(game -> {
								Assertions.assertThat(game.getTitle()).isEqualTo("Travelling Salesman Problem");

								Assertions.assertThat(game.getMinPlayers()).isEqualTo(1);
								Assertions.assertThat(game.getMaxPlayers()).isEqualTo(Integer.MAX_VALUE);
							})
							.anySatisfy(game -> {
								Assertions.assertThat(game.getTitle()).isEqualTo("Tic-Tac-Toe");

								Assertions.assertThat(game.getMinPlayers()).isEqualTo(2);
								Assertions.assertThat(game.getMaxPlayers()).isEqualTo(2);
							});
				});
	}

	@Test
	public void testSearchGames_gameId_undefined() {
		log.debug("About {}", GameSearchHandler.class);

		webTestClient.get()

				.uri("/api/games?game_id=undefined")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()

				.expectStatus()
				.isBadRequest();
	}

	@Test
	public void testSearchGames_gameId_tsp() {
		log.debug("About {}", GameSearchHandler.class);

		webTestClient.get()

				.uri("/api/games?game_id=" + new TravellingSalesmanProblem().getGameMetadata().getGameId())
				.accept(MediaType.APPLICATION_JSON)
				.exchange()

				.expectStatus()
				.isOk();
	}

	@Test
	public void testSearchContests() {
		log.debug("About {}", ContestSearchHandler.class);

		webTestClient.get()
				.uri("/api/contests")
				.accept(MediaType.APPLICATION_JSON)
				.exchange()

				.expectStatus()
				.isOk()
				.expectBodyList(ContestMetadataRaw.class)
				.value(contests -> {
					Assertions.assertThat(contests)
							.hasSize(2)

							.anySatisfy(contest -> {
								Assertions.assertThat(contest.isBeingPlayed()).isFalse();
							});
				});
	}
}