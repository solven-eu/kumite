package eu.solven.kumite.app.it;

import java.time.Duration;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

import eu.solven.kumite.account.fake_player.FakePlayerTokens;
import eu.solven.kumite.account.login.KumiteTokenService;
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
@SpringBootTest(classes = KumiteServerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({ IKumiteSpringProfiles.P_UNSAFE, IKumiteSpringProfiles.P_INMEMORY })
@Slf4j
public class TestKumiteApiRouter {

	@Autowired
	private WebTestClient webTestClient;

	@Autowired
	KumiteTokenService tokenService;

	protected String generateAccessToken() {
		return tokenService.generateAccessToken(FakePlayerTokens.fakeUser(),
				Set.of(FakePlayerTokens.FAKE_PLAYER_ID1),
				Duration.ofMinutes(1));
	}

	@Test
	public void testHello() {
		log.debug("About {}", GreetingHandler.class);

		webTestClient

				.get()
				.uri("/api/hello")
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + generateAccessToken())
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
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + generateAccessToken())
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
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + generateAccessToken())
				.exchange()

				.expectStatus()
				.isBadRequest();
	}

	@Test
	public void testSearchGames_gameId_tsp() {
		log.debug("About {}", GameSearchHandler.class);

		webTestClient.get()

				.uri("/api/games?game_id=" + new TravellingSalesmanProblem().getGameMetadata().getGameId())
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + generateAccessToken())
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
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + generateAccessToken())
				.accept(MediaType.APPLICATION_JSON)
				.exchange()

				.expectStatus()
				.isOk()
				.expectBodyList(ContestMetadataRaw.class)
				.value(contests -> {
					Assertions.assertThat(contests)
							.hasSizeGreaterThanOrEqualTo(2)

							.anySatisfy(contest -> {
								Assertions.assertThat(contest.getDynamicMetadata().isGameOver()).isFalse();
							});
				});
	}
}