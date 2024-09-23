package eu.solven.kumite.app;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import eu.solven.kumite.account.KumiteUsersRegistry;
import eu.solven.kumite.account.fake_player.FakePlayerTokens;
import eu.solven.kumite.app.persistence.InMemoryKumiteConfiguration;
import eu.solven.kumite.app.persistence.RedisKumiteConfiguration;
import eu.solven.kumite.board.BoardHandler;
import eu.solven.kumite.board.BoardLifecycleManager;
import eu.solven.kumite.board.BoardsRegistry;
import eu.solven.kumite.contest.ContestsRegistry;
import eu.solven.kumite.game.GamesRegistry;
import eu.solven.kumite.leaderboard.LeaderboardRegistry;
import eu.solven.kumite.player.ContendersFromBoard;
import eu.solven.kumite.player.ContestPlayersRegistry;
import eu.solven.kumite.player.PlayerMovesHandler;
import eu.solven.kumite.player.PlayersSearchHandler;
import eu.solven.kumite.redis.KumiteRedisConfiguration;
import eu.solven.kumite.tools.KumiteRandomConfiguration;
import eu.solven.kumite.webhook.WebhooksRegistry;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Import({ KumiteRandomConfiguration.class,

		KumiteUsersRegistry.class,
		PlayersSearchHandler.class,
		WebhooksRegistry.class,

		GamesRegistry.class,
		ContestsRegistry.class,
		LeaderboardRegistry.class,

		// LiveContestsRegistry.class,
		ContestPlayersRegistry.class,
		BoardsRegistry.class,
		BoardHandler.class,
		PlayerMovesHandler.class,

		InjectDefaultGamesConfig.class,

		ContendersFromBoard.class,

		// Only one of the following persistence options will actually kicks-in
		InMemoryKumiteConfiguration.class,
		RedisKumiteConfiguration.class,
		KumiteRedisConfiguration.class,

})
@Slf4j
public class KumiteServerComponentsConfiguration {
	@Bean
	public BoardLifecycleManager boardLifecycleManager(BoardsRegistry boardRegistry,
			ContestPlayersRegistry contestPlayersRegistry) {
		final Executor boardEvolutionExecutor = Executors.newFixedThreadPool(4);

		return new BoardLifecycleManager(boardRegistry, contestPlayersRegistry, boardEvolutionExecutor);
	}

	@Profile(IKumiteSpringProfiles.P_FAKEUSER)
	@Bean
	public Void initFakePlayer(KumiteUsersRegistry usersRegistry) {
		log.info("Registering the {} account and players", IKumiteSpringProfiles.P_FAKEUSER);

		usersRegistry.registerOrUpdate(FakePlayerTokens.fakeUser().getRaw());

		return null;
	}
}
