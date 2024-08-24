package eu.solven.kumite.app;

import java.util.Collection;
import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import eu.solven.kumite.board.BoardAndPlayers;
import eu.solven.kumite.board.IKumiteBoard;
import eu.solven.kumite.contest.Contest;
import eu.solven.kumite.contest.ContestMetadata;
import eu.solven.kumite.contest.ContestsStore;
import eu.solven.kumite.contest.LiveContestsManager;
import eu.solven.kumite.game.GamesStore;
import eu.solven.kumite.game.IGame;
import eu.solven.kumite.game.optimization.TravellingSalesmanProblem;
import eu.solven.kumite.player.PlayersList;

@Configuration
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
		games.forEach(c -> gamesStore.registerContest(c));

		return null;
	}


	@Bean
	public LiveContestsManager liveContestsManager() {
		return new LiveContestsManager();
	}

	@Bean
	public ContestsStore contestsStore(LiveContestsManager liveContestsManager) {
		return new ContestsStore(liveContestsManager);
	}

	@Profile(IKumiteSpringProfiles.P_INJECT_DEFAULT_GAMES)
	@Bean
	public Void injectStaticContests(ContestsStore contestsStore, Collection<IGame> games) {
		games.forEach(game -> {
			ContestMetadata contestMeta = ContestMetadata.builder()
					.contestId(UUID.randomUUID())
					.gameMetadata(game.getGameMetadata())
					// .acceptPlayers(true)
					.hasPlayers(PlayersList.builder().build())
					.gameOver(false)
					.build();

			IKumiteBoard initialBoard = game.generateInitialBoard();
			BoardAndPlayers boardAndPlayers = BoardAndPlayers.builder().game(game).board(initialBoard).build();
			Contest contest = Contest.builder().contestMetadata(contestMeta).refBoard(boardAndPlayers).build();

			contestsStore.registerContest(contest);
		});

		return null;
	}
}
