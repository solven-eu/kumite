package eu.solven.kumite.scenario;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.greenrobot.eventbus.EventBus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import eu.solven.kumite.account.fake_player.RandomPlayer;
import eu.solven.kumite.app.IKumiteSpringProfiles;
import eu.solven.kumite.app.KumiteServerComponentsConfiguration;
import eu.solven.kumite.board.BoardsRegistry;
import eu.solven.kumite.board.IHasBoard;
import eu.solven.kumite.contest.ActiveContestGenerator;
import eu.solven.kumite.contest.Contest;
import eu.solven.kumite.contest.ContestsRegistry;
import eu.solven.kumite.contest.IHasGameover;
import eu.solven.kumite.game.GameMetadata;
import eu.solven.kumite.game.GameSearchParameters;
import eu.solven.kumite.game.GamesRegistry;
import eu.solven.kumite.game.IGame;
import eu.solven.kumite.game.IGameMetadataConstants;
import eu.solven.kumite.game.snake.Snake;
import eu.solven.kumite.leaderboard.IPlayerScore;
import eu.solven.kumite.leaderboard.Leaderboard;
import eu.solven.kumite.randomgamer.GamerLogicHelper;
import eu.solven.kumite.randomgamer.RandomPlayersVsThemselves;
import eu.solven.kumite.randomgamer.realtime.RandomRealTimeGamer;

/**
 * This is useful the {@link RandomPlayer} does not trying to play one game move to another game contest.
 * 
 * This does not relies on {@link EventBus} to play RandomPlayer.
 * 
 * @author Benoit Lacelle
 *
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {

		KumiteServerComponentsConfiguration.class,
		ActiveContestGenerator.class,
		RandomRealTimeGamer.class,
		GamerLogicHelper.class,

		RandomPlayersVsThemselves.class,

})
@ActiveProfiles({ IKumiteSpringProfiles.P_INMEMORY })
public class TestRealTimeSoloGame_Manual {

	@Autowired
	GamesRegistry gamesRegistry;

	@Autowired
	ContestsRegistry contestsRegistry;

	@Autowired
	BoardsRegistry boardsRegistry;

	@Autowired
	ActiveContestGenerator activeContestGenerator;

	@Autowired
	RandomRealTimeGamer randomGamer;

	Snake game1 = new Snake();

	@BeforeEach
	public void injectGame() {
		gamesRegistry.registerGame(game1);
	}

	@Test
	public void testGame() throws InterruptedException {
		// Single game
		int nbGames = 1;
		List<GameMetadata> games = gamesRegistry.searchGames(GameSearchParameters.builder().build());
		Assertions.assertThat(games).hasSize(nbGames);

		// Create playable contests:
		Assertions.assertThat(activeContestGenerator.makeContestsIfNoneJoinable()).isEqualTo(nbGames);

		int nbPlayers = games.stream().mapToInt(g -> g.getMinPlayers()).max().getAsInt();
		// int nbPlayersControl = games.stream().mapToInt(g -> g.getMaxPlayers()).min().getAsInt();
		// if (nbPlayers > nbPlayersControl) {
		// throw new IllegalStateException("The selected number of ");
		// }

		// Solo game
		Assertions.assertThat(nbPlayers).isEqualTo(1);

		Map<UUID, Set<UUID>> contestIds = randomGamer
				.joinOncePerContestAndPlayer(GameSearchParameters.builder().build(), new RandomPlayersVsThemselves());
		Assertions.assertThat(contestIds).hasSize(nbGames);

		UUID contestId = contestIds.keySet().iterator().next();

		{
			Set<UUID> playerIds = contestIds.get(contestId);
			Assertions.assertThat(playerIds).hasSizeBetween(1, nbPlayers);
		}
		UUID playerId = contestIds.values().iterator().next().iterator().next();

		Contest contest = contestsRegistry.getContest(contestId);

		Optional<UUID> currentBoardStateId = Optional.empty();
		boolean firstMove = true;
		while (true) {
			if (firstMove) {
				firstMove = false;
			} else if (currentBoardStateId.isEmpty()) {
				// There is not a single playable move
			} else {
				randomGamer.waitBoardUpdate(contestId, currentBoardStateId.get());

				if (isContestPlayed(contest)) {
					// The game may be done after waiting
					// Typically happens on turn-based games (e.g. the other played a winning move)
					// Or real-time games (e.g. the time led to a Gameover)
					break;
				}
			}

			currentBoardStateId = randomGamer.playOnce(contestId, playerId);

			if (isContestPlayed(contest)) {
				break;
			}
		}

		{
			IGame game = contest.getGame();

			IHasBoard hasBoard = contest.getBoard();

			IHasGameover hasGameover = boardsRegistry.hasGameover(game, contest.getContestId());

			if (game.getGameMetadata().getTags().contains(IGameMetadataConstants.TAG_OPTIMIZATION)) {
				// Optimization games can be played indefinitely
				Assertions.assertThat(hasGameover.isGameOver()).isFalse();

				Leaderboard leaderboard = game.makeLeaderboard(hasBoard.get());
				Assertions.assertThat(leaderboard.getPlayerIdToPlayerScore()).hasSize(1);
			} else {
				Assertions.assertThat(hasGameover.isGameOver()).isTrue();

				Leaderboard leaderboard = game.makeLeaderboard(hasBoard.get());
				Assertions.assertThat(leaderboard.getPlayerIdToPlayerScore()).hasSize(nbPlayers);
			}
		}
	}

	public static boolean areContestsPlayed(List<Contest> contests) {
		return contests.stream().allMatch(c -> isContestPlayed(c));
	}

	private static boolean isContestPlayed(Contest g) {
		if (g.isGameOver()) {
			return true;
		} else if (g.getGameMetadata().getTags().contains(IGameMetadataConstants.TAG_OPTIMIZATION)) {
			// Optimization games can be played indefinitely: we consider them played if at least one player played
			Map<UUID, IPlayerScore> leaderboard =
					g.getGame().makeLeaderboard(g.getBoard().get()).getPlayerIdToPlayerScore();
			return leaderboard.values().stream().anyMatch(ps -> ps.asNumber().doubleValue() != 0D);
		} else {
			return false;
		}
	}
}
