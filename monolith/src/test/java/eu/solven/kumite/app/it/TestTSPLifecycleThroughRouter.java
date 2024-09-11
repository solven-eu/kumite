package eu.solven.kumite.app.it;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import eu.solven.kumite.app.IKumiteServer;
import eu.solven.kumite.app.IKumiteSpringProfiles;
import eu.solven.kumite.app.KumiteServerApplication;
import eu.solven.kumite.app.KumiteWebclientServer;
import eu.solven.kumite.contest.ContestSearchParameters;
import eu.solven.kumite.game.GameSearchParameters;
import eu.solven.kumite.player.PlayerRawMovesHolder;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * This integration-test serves 2 purposes: first it shows how one can chain call to play a game: it can help ensure the
 * API is stable and simple; second, it ensures the API is actually functional (e.g. up to serializibility of involved
 * classes).
 * 
 * @author Benoit Lacelle
 * @see 'TestTSPLifecycle'
 */
// Should this move to `monolith` module?
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = KumiteServerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({ IKumiteSpringProfiles.P_DEFAULT, IKumiteSpringProfiles.P_DEFAULT_FAKE_PLAYER, })
@TestPropertySource(properties = { "kumite.random.seed=123",
		"kumite.server.base-url=http://localhost:LocalServerPort",
		"kumite.random.seed=123" })
@Slf4j
public class TestTSPLifecycleThroughRouter {

	// https://stackoverflow.com/questions/30312058/spring-boot-how-to-get-the-running-port
	@LocalServerPort
	int randomServerPort;

	@Autowired
	Environment env;

	@Test
	public void testSinglePlayer() {
		IKumiteServer kumiteServer = new KumiteWebclientServer(env, randomServerPort);

		UUID playerId = env.getRequiredProperty("kumite.playerId", UUID.class);

		kumiteServer
				// Search for games given a human-friendly pattern
				.searchGames(GameSearchParameters.builder().titleRegex(Optional.of(".*Salesman.*")).build())
				// Search for contest
				.flatMap(game -> {
					log.info("Processing game={}", game);
					return kumiteServer.searchContests(
							ContestSearchParameters.builder().gameId(Optional.of(game.getGameId())).build());
				})
				// Filter relevant contests
				.filter(c -> {
					// log.info("c={}", c);
					return true;
				})
				.filter(c -> c.getDynamicMetadata().isAcceptingPlayers())
				.filter(c -> !c.getDynamicMetadata().isGameOver())
				// Join each relevant contest
				.flatMap(contest -> {
					log.info("Joining contest={}", contest);
					return kumiteServer.joinContest(playerId, contest.getContestId()).flatMap(playerPlayer -> {
						log.info("playerPlayer={}", playerPlayer);

						return kumiteServer.loadBoard(playerId, contest.getContestId()).flatMap(joinedContest -> {
							log.info("Received board for contest={}", joinedContest.getContestId());

							Mono<PlayerRawMovesHolder> exampleMoves =
									kumiteServer.getExampleMoves(playerId, joinedContest.getContestId());

							return exampleMoves.flatMap(moves -> {
								Optional<Map<String, ?>> someMove = moves.getMoves().values().stream().findAny();
								return Mono.justOrEmpty(someMove);
							}).flatMap(move -> {
								return kumiteServer.playMove(playerId, joinedContest.getContestId(), move);
							});
						});
					});
				})

				.doOnError(t -> {
					log.error("Something went wrong", t);
				})
				.then()
				.block();
	}
}
