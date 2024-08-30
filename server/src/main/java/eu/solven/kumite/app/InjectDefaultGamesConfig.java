package eu.solven.kumite.app;

import java.time.OffsetDateTime;
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
import eu.solven.kumite.game.GamesStore;
import eu.solven.kumite.game.IGame;
import eu.solven.kumite.player.ContestPlayersRegistry;
import eu.solven.kumite.player.IHasPlayers;

@Configuration
@Profile(IKumiteSpringProfiles.P_INJECT_DEFAULT_GAMES)
public class InjectDefaultGamesConfig {

	@Bean
	public Void injectStaticGames(GamesStore gamesStore, Collection<IGame> games) {
		games.forEach(c -> gamesStore.registerGame(c));

		return null;
	}

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
					.name("Auto-generated " + OffsetDateTime.now())
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
}
