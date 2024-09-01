package eu.solven.kumite.app;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Random;
import java.util.UUID;
import java.util.random.RandomGenerator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import eu.solven.kumite.board.BoardsRegistry;
import eu.solven.kumite.board.IHasBoard;
import eu.solven.kumite.board.IKumiteBoard;
import eu.solven.kumite.contest.Contest;
import eu.solven.kumite.contest.ContestMetadata;
import eu.solven.kumite.contest.ContestsRegistry;
import eu.solven.kumite.game.GamesRegistry;
import eu.solven.kumite.game.IGame;
import eu.solven.kumite.game.opposition.chess.Chess;
import eu.solven.kumite.game.optimization.tsp.TravellingSalesmanProblem;
import eu.solven.kumite.player.ContestPlayersRegistry;
import eu.solven.kumite.player.IHasPlayers;

@Configuration
@Profile(IKumiteSpringProfiles.P_INJECT_DEFAULT_GAMES)
public class InjectDefaultGamesConfig {

	@Autowired
	BoardsRegistry boardRegistry;

	@Bean
	TravellingSalesmanProblem tsm() {
		return new TravellingSalesmanProblem();
	}

	@Bean
	Chess chess() {
		return new Chess();
	}

	@Bean
	public Void injectStaticGames(GamesRegistry gamesStore, Collection<IGame> games) {
		games.forEach(c -> gamesStore.registerGame(c));

		return null;
	}

	@Bean
	public Void injectStaticContests(ContestsRegistry contestsStore,
			ContestPlayersRegistry playersRegistry,
			Collection<IGame> games,
			Environment env) {
		String rawSeed = env.getProperty("kumite.random.seed", "random");
		RandomGenerator r;
		if ("random".equals(rawSeed)) {
			r = new Random();
		} else {
			r = new Random(Integer.parseInt(rawSeed));
		}

		games.forEach(game -> {
			UUID contestId = UUID.randomUUID();
			IHasPlayers players = playersRegistry.makeDynamicHasPlayers(contestId);

			ContestMetadata contestMeta = ContestMetadata.builder()
					.contestId(contestId)
					.gameMetadata(game.getGameMetadata())
					.name("Auto-generated " + OffsetDateTime.now().format(DateTimeFormatter.ISO_INSTANT))
					// .acceptPlayers(true)
					.hasPlayers(players)
					.gameOver(false)
					.build();

			IKumiteBoard initialBoard = game.generateInitialBoard(r);

			boardRegistry.registerBoard(contestId, initialBoard);

			IHasBoard hasBoard = boardRegistry.makeDynamicBoardHolder(contestId);

			Contest contest = Contest.builder()
					.contestMetadata(contestMeta)
					.game(game)
					.board(hasBoard)
					.hasPlayers(players)
					.build();

			contestsStore.registerContest(contest);
		});

		return null;
	}
}
