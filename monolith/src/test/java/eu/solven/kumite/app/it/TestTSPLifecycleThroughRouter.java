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

import eu.solven.kumite.account.fake_player.FakePlayerTokens;
import eu.solven.kumite.app.IKumiteSpringProfiles;
import eu.solven.kumite.app.KumiteContestServerApplication;
import eu.solven.kumite.app.KumiteWebclientServerProperties;
import eu.solven.kumite.app.server.IKumiteServer;
import eu.solven.kumite.app.server.KumiteWebclientServer;
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
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = KumiteContestServerApplication.class,
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({ IKumiteSpringProfiles.P_UNSAFE, IKumiteSpringProfiles.P_INMEMORY, IKumiteSpringProfiles.P_FAKEUSER })
@TestPropertySource(properties = { "kumite.random.seed=123",
		"kumite.player.wait_duration_if_no_move" + "=PT0.001S",
		KumiteWebclientServerProperties.KEY_PLAYER_CONTESTBASEURL + "=http://localhost:LocalServerPort" })
@Slf4j
public class TestTSPLifecycleThroughRouter {

	// https://stackoverflow.com/questions/30312058/spring-boot-how-to-get-the-running-port
	@LocalServerPort
	int randomServerPort;

	@Autowired
	Environment env;

	@Test
	public void testSinglePlayer() {
		UUID playerId = FakePlayerTokens.FAKE_PLAYER_ID1;

		KumiteWebclientServerProperties properties = KumiteWebclientServerProperties.forTests(env, randomServerPort);
		IKumiteServer kumiteServer = KumiteWebclientServer.fromProperties(properties);

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
