package eu.solven.kumite.app;

import java.util.Collection;
import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import eu.solven.kumite.account.AccountsStore;
import eu.solven.kumite.account.login.KumiteUsersRegistry;
import eu.solven.kumite.board.BoardAndPlayers;
import eu.solven.kumite.board.IKumiteBoard;
import eu.solven.kumite.contest.Contest;
import eu.solven.kumite.contest.ContestLifecycleManager;
import eu.solven.kumite.contest.ContestMetadata;
import eu.solven.kumite.contest.ContestsStore;
import eu.solven.kumite.contest.LiveContestsManager;
import eu.solven.kumite.game.GamesStore;
import eu.solven.kumite.game.IGame;
import eu.solven.kumite.game.optimization.TravellingSalesmanProblem;
import eu.solven.kumite.leaderboard.LeaderboardRegistry;
import eu.solven.kumite.player.ContestPlayersRegistry;
import eu.solven.kumite.player.IHasPlayers;
import eu.solven.kumite.webhook.WebhooksRegistry;

@Configuration
@Import({

		KumiteUsersRegistry.class,

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

	@Profile(IKumiteSpringProfiles.P_INJECT_DEFAULT_GAMES)
	@Bean
	public Void injectStaticGames(GamesStore gamesStore, Collection<IGame> games) {
		games.forEach(c -> gamesStore.registerGame(c));

		return null;
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

	@Profile(IKumiteSpringProfiles.P_INJECT_DEFAULT_GAMES)
	@Bean
	public Void injectStaticContests(ContestsStore contestsStore,
			ContestPlayersRegistry playersRegistry,
			Collection<IGame> games) {
		games.forEach(game -> {
			UUID contestId = UUID.randomUUID();
			IHasPlayers players = playersRegistry.makeDynamicHasPlayers(contestId);

			ContestMetadata contestMeta = ContestMetadata.builder()
					.contestId(contestId)
					.gameMetadata(game.getGameMetadata())
					// .acceptPlayers(true)
					.hasPlayers(players)
					.gameOver(false)
					.build();

			IKumiteBoard initialBoard = game.generateInitialBoard();
			BoardAndPlayers boardAndPlayers =
					BoardAndPlayers.builder().game(game).board(initialBoard).hasPlayers(players).build();
			Contest contest = Contest.builder().contestMetadata(contestMeta).refBoard(boardAndPlayers).build();

			contestsStore.registerContest(contest);
		});

		return null;
	}

	@Bean
	public AccountsStore accountsStore() {
		return new AccountsStore();
	}

	@Bean
	public LeaderboardRegistry leaderboardRegistry() {
		return new LeaderboardRegistry();
	}

	@Bean
	public WebhooksRegistry webhooksRegistry() {
		return new WebhooksRegistry();
	}

}
