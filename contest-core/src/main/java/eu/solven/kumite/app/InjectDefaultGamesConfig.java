package eu.solven.kumite.app;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import eu.solven.kumite.board.BoardsRegistry;
import eu.solven.kumite.contest.ActiveContestGenerator;
import eu.solven.kumite.eventbus.IEventSubscriber;
import eu.solven.kumite.game.GamesRegistry;
import eu.solven.kumite.game.IGame;
import eu.solven.kumite.game.opposition.tictactoe.TicTacToe;
import eu.solven.kumite.game.optimization.lag.Lag;
import eu.solven.kumite.game.optimization.tsp.TravellingSalesmanProblem;
import eu.solven.kumite.tools.CloseableBean;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Profile(IKumiteSpringProfiles.P_INJECT_DEFAULT_GAMES)
@Import({

		ActiveContestGenerator.class,

})
@Slf4j
public class InjectDefaultGamesConfig {
	// Enable not injecting synchronously at boot
	public static final String KEY_INJECTDEFAULTCONTESTS_SYNCHRONOUS =
			"kumite." + IKumiteSpringProfiles.P_INJECT_DEFAULT_GAMES + ".synchronous";
	public static final String KEY_INJECTDEFAULTCONTESTS_PERIOD =
			"kumite." + IKumiteSpringProfiles.P_INJECT_DEFAULT_GAMES + ".period";

	@Autowired
	BoardsRegistry boardRegistry;

	@Bean
	TravellingSalesmanProblem tsm() {
		return new TravellingSalesmanProblem();
	}

	@Bean
	TicTacToe ticTacToe() {
		return new TicTacToe();
	}

	@Bean
	Lag lag() {
		return new Lag();
	}

	// Chess is not ready, and is quite complex to implement. It would need to rely on some library (e.g.
	// https://github.com/bhlangonijr/chesslib) which may be preferably done through a separate module (e.g. or else
	// through a Game by API, which the game being externalized)
	// @Bean
	// Chess chess() {
	// return new Chess();
	// }

	@Bean
	public Void injectStaticGames(GamesRegistry gamesStore, Collection<IGame> games) {
		games.forEach(c -> gamesStore.registerGame(c));

		return null;
	}

	@Bean
	public CloseableBean injectStaticContestsInitial(Environment env,
			ActiveContestGenerator activeContestGenerator,
			// We must ensure all eventSubscribers are registered before doing action generating event
			List<IEventSubscriber> eventsSubscribers) {
		log.debug("Now {} are registered, we can do stuff generating events", eventsSubscribers);

		if (env.getProperty(KEY_INJECTDEFAULTCONTESTS_SYNCHRONOUS, Boolean.class, true)) {
			// This is useful for unitTests, to get contests right-away.
			// it is OK for PRD as this generation is supposedly very fast
			log.info("We generate contests synchronously");
			activeContestGenerator.makeContestsIfNoneJoinable();
		}

		return null;
	}

	@Bean
	public CloseableBean injectStaticContestsSchedule(Environment env, ActiveContestGenerator activeContestGenerator) {
		Duration periodEnsureActiveContests =
				env.getProperty(KEY_INJECTDEFAULTCONTESTS_PERIOD, Duration.class, Duration.ofSeconds(15));
		log.info("Contests for games without joinable contest will be generated every {}", periodEnsureActiveContests);
		long seconds = periodEnsureActiveContests.toSeconds();

		ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
		ses.scheduleWithFixedDelay(() -> {
			// BEWARE The appContext may not be fully started yet
			// https://stackoverflow.com/questions/8686507/how-to-add-a-hook-to-the-application-context-initialization-event
			// https://www.baeldung.com/spring-context-events

			log.debug("About to generate contests for games without joinable contest");
			activeContestGenerator.makeContestsIfNoneJoinable();
		}, seconds, seconds, TimeUnit.SECONDS);

		// In case of reboot (e.g. DevTools), we need to make sure the executor is closed
		return new CloseableBean(ses);
	}
}
