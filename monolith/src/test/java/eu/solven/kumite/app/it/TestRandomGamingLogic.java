package eu.solven.kumite.app.it;

import java.time.Duration;
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

import eu.solven.kumite.account.fake_player.FakePlayerTokens;
import eu.solven.kumite.account.login.KumiteTokenService;
import eu.solven.kumite.app.IKumiteSpringProfiles;
import eu.solven.kumite.app.KumiteServerApplication;
import eu.solven.kumite.app.player.IGamingLogic;
import eu.solven.kumite.app.player.RandomGamingLogic;
import eu.solven.kumite.app.server.IKumiteServer;
import eu.solven.kumite.app.server.KumiteWebclientServer;
import eu.solven.kumite.contest.ContestView;
import eu.solven.kumite.tools.JdkUuidGenerator;
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
@SpringBootTest(classes = KumiteServerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({ IKumiteSpringProfiles.P_UNSAFE, IKumiteSpringProfiles.P_INMEMORY })
@TestPropertySource(properties = { "kumite.random.seed=123",
		// "kumite.playerId=11111111-1111-1111-1111-111111111111",
		"kumite.server.base-url=http://localhost:LocalServerPort" })
@Slf4j
public class TestRandomGamingLogic {

	// https://stackoverflow.com/questions/30312058/spring-boot-how-to-get-the-running-port
	@LocalServerPort
	int randomServerPort;

	@Autowired
	Environment env;

	@Test
	public void testOptimization() {
		UUID playerId = FakePlayerTokens.FAKE_PLAYER_ID1;
		KumiteTokenService kumiteTokenService = new KumiteTokenService(env, new JdkUuidGenerator());
		String accessToken = kumiteTokenService
				.generateAccessToken(FakePlayerTokens.fakeUser(), Set.of(playerId), Duration.ofMinutes(1), false);

		IKumiteServer kumiteServer = new KumiteWebclientServer(env, randomServerPort, accessToken);

		IGamingLogic kumitePlayer = new RandomGamingLogic(kumiteServer);

		Set<UUID> contestIds = kumitePlayer.playOptimizationGames(playerId);

		Assertions.assertThat(contestIds).hasSizeGreaterThanOrEqualTo(1);
	}

	@Test
	public void test1v1TurnBased() throws InterruptedException {
		// We're playing 1v1
		int nbPlayers = 1 + 1;

		// We will play 2 players concurrently
		ExecutorService executorService = Executors.newFixedThreadPool(2);

		KumiteTokenService kumiteTokenService = new KumiteTokenService(env, new JdkUuidGenerator());

		AtomicReference<Throwable> asyncThrowable = new AtomicReference<>();

		CountDownLatch cdl = new CountDownLatch(nbPlayers);

		Set<UUID> contestIds = new ConcurrentSkipListSet<>();

		for (int iPlayer = 0; iPlayer < nbPlayers; iPlayer++) {
			UUID playerId = FakePlayerTokens.fakePlayerId(iPlayer);
			String accessToken = kumiteTokenService
					.generateAccessToken(FakePlayerTokens.fakeUser(), Set.of(playerId), Duration.ofMinutes(1), false);

			IKumiteServer kumiteServer = new KumiteWebclientServer(env, randomServerPort, accessToken);
			IGamingLogic kumitePlayer = new RandomGamingLogic(kumiteServer);

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
