package eu.solven.kumite.app.it;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import eu.solven.kumite.account.fake_player.RandomPlayer;
import eu.solven.kumite.app.IKumiteSpringProfiles;
import eu.solven.kumite.app.KumiteContestServerApplication;
import eu.solven.kumite.app.KumiteWebclientServerProperties;
import eu.solven.kumite.app.player.IGamingLogic;
import eu.solven.kumite.app.player.RandomGamingLogic;
import eu.solven.kumite.app.server.IKumiteServer;
import eu.solven.kumite.app.server.KumiteWebclientServer;
import eu.solven.kumite.contest.ContestView;
import lombok.extern.slf4j.Slf4j;

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
@ActiveProfiles({ IKumiteSpringProfiles.P_UNSAFE, IKumiteSpringProfiles.P_INMEMORY })
@TestPropertySource(properties = { "kumite.random.seed=123",
		"kumite.player.wait_duration_if_no_move" + "=PT0.001S",

		KumiteWebclientServerProperties.KEY_PLAYER_CONTESTBASEURL + "=http://localhost:LocalServerPort" })
@Slf4j
public class TestRandomGamingLogic {

	// https://stackoverflow.com/questions/30312058/spring-boot-how-to-get-the-running-port
	@LocalServerPort
	int randomServerPort;

	@Autowired
	Environment env;

	@Test
	public void testOptimization() {
		UUID playerId = RandomPlayer.PLAYERID_1;

		KumiteWebclientServerProperties properties = KumiteWebclientServerProperties.forTests(env, randomServerPort);
		KumiteWebclientServer kumiteServer = KumiteWebclientServer.fromProperties(properties);

		RandomGamingLogic kumitePlayer = new RandomGamingLogic(env, kumiteServer);

		Set<UUID> contestIds = kumitePlayer.playOptimizationGames(playerId);

		Assertions.assertThat(contestIds).hasSizeGreaterThanOrEqualTo(1);

		// This should have its dedicated unitTest
		Assertions.assertThat(kumiteServer.getNbAccessTokens()).isEqualTo(1);
	}

	@Test
	public void test1v1TurnBased() throws InterruptedException {
		// We're playing 1v1
		int nbPlayers = 1 + 1;

		// We will play 2 players concurrently
		ExecutorService executorService = Executors.newFixedThreadPool(2);

		AtomicReference<Throwable> asyncThrowable = new AtomicReference<>();

		CountDownLatch cdl = new CountDownLatch(nbPlayers);

		Set<UUID> contestIds = new ConcurrentSkipListSet<>();

		KumiteWebclientServerProperties properties = KumiteWebclientServerProperties.forTests(env, randomServerPort);
		IKumiteServer kumiteServer = KumiteWebclientServer.fromProperties(properties);
		IGamingLogic kumitePlayer = new RandomGamingLogic(env, kumiteServer);

		for (int iPlayer = 0; iPlayer < nbPlayers; iPlayer++) {
			UUID playerId = RandomPlayer.playerId(iPlayer);

			executorService.execute(() -> {
				try {
					log.info("We start playing playerId={}", playerId);
					Set<UUID> playerContestIds = kumitePlayer.play1v1TurnBasedGames(playerId);

					for (UUID contestId : contestIds) {
						ContestView board = kumiteServer.loadBoard(playerId, contestId).block();
						Assertions.assertThat(board.getDynamicMetadata().isGameOver()).isTrue();
					}

					contestIds.addAll(playerContestIds);
				} catch (Throwable t) {
					asyncThrowable.compareAndSet(null, t);
				} finally {
					cdl.countDown();
				}
			});
		}

		if (asyncThrowable.get() != null) {
			throw new IllegalStateException("Issue while playing", asyncThrowable.get());
		} else if (!cdl.await(1, TimeUnit.MINUTES)) {
			throw new IllegalStateException("Playing games is taking too long");
		}

		Assertions.assertThat(contestIds).hasSizeGreaterThanOrEqualTo(1);
	}
}
