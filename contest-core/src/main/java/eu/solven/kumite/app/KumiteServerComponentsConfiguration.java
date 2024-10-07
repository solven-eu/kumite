package eu.solven.kumite.app;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import eu.solven.kumite.account.KumiteUsersRegistry;
import eu.solven.kumite.app.automated.KumiteAutomatedSpringConfig;
import eu.solven.kumite.app.persistence.InMemoryKumiteConfiguration;
import eu.solven.kumite.board.BoardLifecycleManager;
import eu.solven.kumite.board.BoardLifecycleManagerHelper;
import eu.solven.kumite.board.BoardsRegistry;
import eu.solven.kumite.board.realtime.RealTimeBoardManager;
import eu.solven.kumite.contest.ContestsRegistry;
import eu.solven.kumite.eventbus.ActivityLogger;
import eu.solven.kumite.eventbus.EventBusLogger;
import eu.solven.kumite.eventbus.EventSubscriber;
import eu.solven.kumite.game.GamesRegistry;
import eu.solven.kumite.game.IGameMetadataConstants;
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

		ContendersFromBoard.class,

		// Only one of the following persistence options will actually kicks-in
		InMemoryKumiteConfiguration.class,

		// Should introduce a Redis version
		InMemoryViewingAccountsRepository.class,

		KumiteAutomatedSpringConfig.class,

		BoardLifecycleManagerHelper.class,

})
@Slf4j
public class KumiteServerComponentsConfiguration {

	@Bean
	@Qualifier(IGameMetadataConstants.TAG_TURNBASED)
	public BoardLifecycleManager tbBoardLifecycleManager(BoardLifecycleManagerHelper helper) {
		final Executor boardEvolutionExecutor = Executors.newFixedThreadPool(4);
		boardEvolutionExecutor
				.execute(() -> log.info("This flags the tbThreadPool threadName={}", Thread.currentThread().getName()));

		return new BoardLifecycleManager(helper, boardEvolutionExecutor);
	}

	// We can have multiple BoardLifecycleManager/boardEvolutionExecutor as long as a given contest is guaranteed to be
	// processed by a single executor
	@Bean
	@Qualifier(IGameMetadataConstants.TAG_REALTIME)
	public RealTimeBoardManager rtBoardLifecycleManager(BoardLifecycleManagerHelper helper) {
		final Executor boardEvolutionExecutor = Executors.newFixedThreadPool(4);
		boardEvolutionExecutor
				.execute(() -> log.info("This flags the rtThreadPool threadName={}", Thread.currentThread().getName()));

		return new RealTimeBoardManager(helper, boardEvolutionExecutor);
	}

	@Bean
	public EventSubscriber registerRtBoardManagerInEventBus(RealTimeBoardManager rtBoardManager, EventBus eventBus) {
		return new EventSubscriber(eventBus, rtBoardManager);
	}

	@Bean
	public EventBus eventBus() {
		EventBus eventBus = EventBus.builder()
				.strictMethodVerification(true)
				.throwSubscriberException(true)
				.logger(makeLogger())
				.build();

		eventBus.register(new EventBusLogger());

		return eventBus;
	}

	@Bean
	public EventSubscriber activityLogger(EventBus eventBus) {
		return new EventSubscriber(eventBus, new ActivityLogger());
	}

	private Logger makeLogger() {
		return new EventBusLogger();
	}
}
