package eu.solven.kumite.app;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import eu.solven.kumite.account.login.KumiteUsersRegistry;
import eu.solven.kumite.board.BoardHandler;
import eu.solven.kumite.board.BoardsRegistry;
import eu.solven.kumite.contest.ContestsRegistry;
import eu.solven.kumite.contest.LiveContestsRegistry;
import eu.solven.kumite.game.GamesRegistry;
import eu.solven.kumite.leaderboard.LeaderboardRegistry;
import eu.solven.kumite.lifecycle.BoardLifecycleManager;
import eu.solven.kumite.lifecycle.ContestLifecycleManager;
import eu.solven.kumite.player.AccountPlayersRegistry;
import eu.solven.kumite.player.ContestPlayersRegistry;
import eu.solven.kumite.player.PlayerMovesHandler;
import eu.solven.kumite.player.PlayersSearchHandler;
import eu.solven.kumite.webhook.WebhooksRegistry;

@Configuration
@Import({

		// AccountsStore.class,
		KumiteUsersRegistry.class,
		PlayersSearchHandler.class,
		AccountPlayersRegistry.class,
		WebhooksRegistry.class,

		GamesRegistry.class,
		ContestsRegistry.class,
		LeaderboardRegistry.class,

		LiveContestsRegistry.class,
		ContestPlayersRegistry.class,
		BoardsRegistry.class,
		BoardHandler.class,
		PlayerMovesHandler.class,

		ContestLifecycleManager.class,

		InjectDefaultGamesConfig.class,

})
public class KumiteServerComponentsConfiguration {
	@Bean
	public BoardLifecycleManager boardLifecycleManager(BoardsRegistry boardRegistry,
			ContestPlayersRegistry contestPlayersRegistry) {
		final Executor boardEvolutionExecutor = Executors.newFixedThreadPool(4);

		return new BoardLifecycleManager(boardRegistry, contestPlayersRegistry, boardEvolutionExecutor);

	}
}
