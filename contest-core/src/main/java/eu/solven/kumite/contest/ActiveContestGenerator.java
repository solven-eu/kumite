package eu.solven.kumite.contest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.random.RandomGenerator;

import eu.solven.kumite.account.fake_player.RandomPlayer;
import eu.solven.kumite.app.IKumiteSpringProfiles;
import eu.solven.kumite.board.IKumiteBoard;
import eu.solven.kumite.game.GameSearchParameters;
import eu.solven.kumite.game.GamesRegistry;
import eu.solven.kumite.game.IGame;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The goal if this class is to ensure each game always has at least one active contest. By active we mean: neither
 * gameOver, and accepting players (possibly not requiring players). It means each game should be easily played for any
 * new player.
 * 
 * @author Benoit Lacelle
 *
 */
@AllArgsConstructor
@Slf4j
public class ActiveContestGenerator {
	final GamesRegistry gamesRegistry;
	final ContestsRegistry contestsRegistry;
	final RandomGenerator randomGenerator;

	public void makeContestsIfNoneJoinable() {
		gamesRegistry.searchGames(GameSearchParameters.builder().build()).forEach(gameMetadata -> {
			UUID gameId = gameMetadata.getGameId();

			List<Contest> activeAndJoinableContests = contestsRegistry.searchContests(ContestSearchParameters.builder()
					.gameId(Optional.of(gameId))
					.acceptPlayers(true)
					.gameOver(false)
					.build());

			if (!activeAndJoinableContests.isEmpty()) {
				log.debug("There is {} active+joinable contests for game={}",
						activeAndJoinableContests.size(),
						gameMetadata.getTitle());
			} else {
				IGame game = gamesRegistry.getGame(gameId);
				IKumiteBoard initialBoard = game.generateInitialBoard(randomGenerator);

				// We suffix with a relatively small number, to easily remember them (as human)
				String contestName = "Auto-generated " + randomGenerator.nextInt(128);
				ContestCreationMetadata constantMetadata = ContestCreationMetadata.fromGame(gameMetadata)
						.name(contestName)
						.author(RandomPlayer.ACCOUNT_ID)
						.build();
				Contest contest = contestsRegistry.registerContest(game, constantMetadata, initialBoard);

				log.info("{} generated contestId={} for gameId={}",
						IKumiteSpringProfiles.P_INJECT_DEFAULT_GAMES,
						contest.getContestId(),
						gameId);
			}
		});
	}
}
