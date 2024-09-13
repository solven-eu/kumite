package eu.solven.kumite.app;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.random.RandomGenerator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import eu.solven.kumite.board.BoardsRegistry;
import eu.solven.kumite.board.IKumiteBoard;
import eu.solven.kumite.contest.Contest;
import eu.solven.kumite.contest.ContestCreationMetadata;
import eu.solven.kumite.contest.ContestsRegistry;
import eu.solven.kumite.game.GamesRegistry;
import eu.solven.kumite.game.IGame;
import eu.solven.kumite.game.opposition.tictactoe.TicTacToe;
import eu.solven.kumite.game.optimization.ping.Lag;
import eu.solven.kumite.game.optimization.tsp.TravellingSalesmanProblem;
import eu.solven.kumite.player.ContestPlayersRegistry;
import eu.solven.kumite.tools.IUuidGenerator;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Profile(IKumiteSpringProfiles.P_INJECT_DEFAULT_GAMES)
@Slf4j
public class InjectDefaultGamesConfig {

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
	public Void injectStaticContests(ContestsRegistry contestsRegistry,
			ContestPlayersRegistry playersRegistry,
			Collection<IGame> games,
			RandomGenerator randomGenerator,
			IUuidGenerator uuidGenerator) {

		games.forEach(game -> {
			IKumiteBoard initialBoard = game.generateInitialBoard(randomGenerator);

			Contest contest = contestsRegistry.registerContest(game,
					ContestCreationMetadata.fromGame(game.getGameMetadata())
							.name("Auto-generated " + OffsetDateTime.now().format(DateTimeFormatter.ISO_INSTANT))
							.build(),
					initialBoard);

			log.info("{} generated {}", IKumiteSpringProfiles.P_INJECT_DEFAULT_GAMES, contest);
		});

		return null;
	}
}
