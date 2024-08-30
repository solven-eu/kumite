package eu.solven.kumite.app;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import eu.solven.kumite.account.AccountsStore;
import eu.solven.kumite.account.login.KumiteUsersRegistry;
import eu.solven.kumite.contest.ContestLifecycleManager;
import eu.solven.kumite.contest.ContestsStore;
import eu.solven.kumite.contest.LiveContestsManager;
import eu.solven.kumite.game.GamesStore;
import eu.solven.kumite.game.optimization.TravellingSalesmanProblem;
import eu.solven.kumite.leaderboard.LeaderboardRegistry;
import eu.solven.kumite.player.AccountPlayersRegistry;
import eu.solven.kumite.player.ContestPlayersRegistry;
import eu.solven.kumite.player.PlayersSearchHandler;
import eu.solven.kumite.webhook.WebhooksRegistry;

@Configuration
@Import({

		AccountsStore.class,
		KumiteUsersRegistry.class,
		PlayersSearchHandler.class,
		AccountPlayersRegistry.class,
		WebhooksRegistry.class,

		InjectDefaultGamesConfig.class,

})
public class KumiteServerComponentsConfiguration {

	@Bean
	public GamesStore gamesStore() {
		return new GamesStore();
	}

	@Bean
	TravellingSalesmanProblem tsm() {
		return new TravellingSalesmanProblem();
	}

	@Bean
	public LiveContestsManager liveContestsManager() {
		return new LiveContestsManager();
	}

	@Bean
	public ContestLifecycleManager contestLifecycleManager(GamesStore gamesStore,
			ContestsStore contestsStore,
			ContestPlayersRegistry contestPlayersRegistry) {
		return ContestLifecycleManager.builder()
				.gamesStore(gamesStore)
				.contestsStore(contestsStore)
				.contestPlayersRegistry(contestPlayersRegistry)
				.build();
	}

	@Bean
	public ContestPlayersRegistry contestPlayersRegistry(GamesStore gamesStore) {
		return new ContestPlayersRegistry(gamesStore);
	}

	@Bean
	public ContestsStore contestsStore(LiveContestsManager liveContestsManager) {
		return new ContestsStore(liveContestsManager);
	}

	@Bean
	public LeaderboardRegistry leaderboardRegistry() {
		return new LeaderboardRegistry();
	}

}
