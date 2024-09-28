package eu.solven.kumite.app;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import eu.solven.kumite.account.KumiteUsersRegistry;
import eu.solven.kumite.app.persistence.InMemoryKumiteConfiguration;
import eu.solven.kumite.board.BoardLifecycleManager;
import eu.solven.kumite.board.BoardsRegistry;
import eu.solven.kumite.contest.ContestsRegistry;
import eu.solven.kumite.game.GamesRegistry;
import eu.solven.kumite.leaderboard.LeaderboardRegistry;
import eu.solven.kumite.player.ContendersFromBoard;
import eu.solven.kumite.player.ContestPlayersRegistry;
import eu.solven.kumite.player.InMemoryViewingAccountsRepository;
import eu.solven.kumite.tools.KumiteRandomConfiguration;
import eu.solven.kumite.webhook.WebhooksRegistry;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Import({

		KumiteRandomConfiguration.class,

		KumiteUsersRegistry.class,

		WebhooksRegistry.class,

		GamesRegistry.class,
		ContestsRegistry.class,
		LeaderboardRegistry.class,

		// LiveContestsRegistry.class,
		ContestPlayersRegistry.class,
		BoardsRegistry.class,

		InjectDefaultGamesConfig.class,
		InjectKumiteAccountsConfig.class,

		ContendersFromBoard.class,

		// Only one of the following persistence options will actually kicks-in
		InMemoryKumiteConfiguration.class,

		// Should introduce a Redis version
		InMemoryViewingAccountsRepository.class,

})
@Slf4j
public class KumiteServerComponentsConfiguration {
	@Bean
	public BoardLifecycleManager boardLifecycleManager(BoardsRegistry boardRegistry,
			ContestPlayersRegistry contestPlayersRegistry) {
		final Executor boardEvolutionExecutor = Executors.newFixedThreadPool(4);

		return new BoardLifecycleManager(boardRegistry, contestPlayersRegistry, boardEvolutionExecutor);
	}

	@Bean
	public EventBus eventBus() {
		return EventBus.builder()
				.strictMethodVerification(true)
				.throwSubscriberException(true)
				.logger(makeLogger())
				.build();
	}

	private Logger makeLogger() {
		return new Logger() {

			@Override
			public void log(Level level, String msg) {
				if (level == Level.SEVERE) {
					log.error("{}", msg);
				} else if (level == Level.WARNING) {
					log.warn("{}", msg);
				} else if (level == Level.INFO) {
					log.info("{}", msg);
				} else if (level == Level.FINE) {
					log.debug("{}", msg);
				} else if (level == Level.FINER || level == Level.FINEST) {
					log.trace("{}", msg);
				} else {
					log.error("Unmanaged level={}. Original message: {}", level, msg);
				}
			}

			@Override
			public void log(Level level, String msg, Throwable t) {
				if (level == Level.SEVERE) {
					log.error("{}", msg, t);
				} else if (level == Level.WARNING) {
					log.warn("{}", msg, t);
				} else if (level == Level.INFO) {
					log.info("{}", msg, t);
				} else if (level == Level.FINE) {
					log.debug("{}", msg, t);
				} else if (level == Level.FINER || level == Level.FINEST) {
					log.trace("{}", msg, t);
				} else {
					log.error("Unmanaged level={}. Original message: {}", level, msg, t);
				}
			}

		};
	}
}
