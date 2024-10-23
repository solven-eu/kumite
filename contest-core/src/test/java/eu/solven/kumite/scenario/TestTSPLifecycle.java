package eu.solven.kumite.scenario;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.random.RandomGenerator;

import org.assertj.core.api.Assertions;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import eu.solven.kumite.account.KumiteUsersRegistry;
import eu.solven.kumite.account.internal.KumiteUser;
import eu.solven.kumite.account.login.IKumiteTestConstants;
import eu.solven.kumite.app.IKumiteSpringProfiles;
import eu.solven.kumite.app.KumiteServerComponentsConfiguration;
import eu.solven.kumite.board.BoardLifecycleManager;
import eu.solven.kumite.contest.Contest;
import eu.solven.kumite.contest.ContestSearchParameters;
import eu.solven.kumite.contest.ContestsRegistry;
import eu.solven.kumite.game.GameMetadata;
import eu.solven.kumite.game.GameSearchParameters;
import eu.solven.kumite.game.GamesRegistry;
import eu.solven.kumite.game.optimization.tsp.TSPBoard;
import eu.solven.kumite.game.optimization.tsp.TSPSolution;
import eu.solven.kumite.game.optimization.tsp.TravellingSalesmanProblem;
import eu.solven.kumite.leaderboard.LeaderboardRaw;
import eu.solven.kumite.leaderboard.LeaderboardRegistry;
import eu.solven.kumite.leaderboard.LeaderboardSearchParameters;
import eu.solven.kumite.move.IKumiteMove;
import eu.solven.kumite.move.PlayerMoveRaw;
import eu.solven.kumite.player.ContestPlayersRegistry;
import eu.solven.kumite.player.KumitePlayer;
import eu.solven.kumite.player.PlayerJoinRaw;

/**
 * 
 * @author Benoit Lacelle
 * @see TestTSPLifecycleThroughRouter
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { KumiteServerComponentsConfiguration.class })
@ActiveProfiles({ IKumiteSpringProfiles.P_INJECT_DEFAULT_GAMES, IKumiteSpringProfiles.P_INMEMORY })
@TestPropertySource(properties = "kumite.random.seed=123")
public class TestTSPLifecycle {

	@Autowired
	ContestPlayersRegistry contestPlayersRegistry;

	@Autowired
	KumiteUsersRegistry usersRegistry;

	@Autowired
	GamesRegistry gamesStore;

	@Autowired
	ContestsRegistry contestsStore;

	@Autowired
	LeaderboardRegistry leaderboardRegistry;

	@Autowired
	BoardLifecycleManager boardLifecycleManager;

	@Autowired
	RandomGenerator randomGenerator;

	@Test
	public void testSinglePlayer() {
		KumiteUser account = usersRegistry.registerOrUpdate(IKumiteTestConstants.userPreRegister());
		UUID accountId = account.getAccountId();

		List<GameMetadata> games =
				gamesStore.searchGames(GameSearchParameters.builder().titleRegex(Optional.of(".*Salesman.*")).build());

		Assertions.assertThat(games).hasSize(1);

		List<Contest> contests = contestsStore.searchContests(
				ContestSearchParameters.builder().gameId(Optional.of(games.get(0).getGameId())).build());

		Assertions.assertThat(contests)
				.hasSize(1)
				.element(0)
				.matches(c -> c.isAcceptingPlayers())
				.matches(c -> !c.isGameOver());

		Contest contest = contestsStore.getContest(contests.get(0).getContestId());

		KumitePlayer player = usersRegistry.getAccountMainPlayer(accountId);
		Assertions.assertThat(player.getPlayerId()).isEqualTo(account.getPlayerId());

		contestPlayersRegistry.registerPlayer(contest,
				PlayerJoinRaw.builder().contestId(contest.getContestId()).playerId(player.getPlayerId()).build());

		TSPBoard tspBoard = (TSPBoard) contest.getBoard().get();
		Map<String, IKumiteMove> nameToMove = new TravellingSalesmanProblem()
				.exampleMoves(randomGenerator, tspBoard.asView(player.getPlayerId()), accountId);
		TSPSolution rawMove = (TSPSolution) nameToMove.get("lexicographical");

		PlayerMoveRaw playerMove = PlayerMoveRaw.builder().playerId(player.getPlayerId()).move(rawMove).build();
		boardLifecycleManager.onPlayerMove(contest, playerMove);

		LeaderboardRaw leaderboard = leaderboardRegistry
				.searchLeaderboard(LeaderboardSearchParameters.builder().contestId(contest.getContestId()).build());

		Assertions.assertThat(leaderboard.getPlayerScores()).hasSize(1);
		Assertions.assertThat(leaderboard.getPlayerScores())

				// lexicographical
				.anySatisfy(ps -> {
					Assertions.assertThat(ps.getPlayerId()).isEqualTo(player.getPlayerId());
					Assertions.assertThat(ps.getScore().doubleValue()).isCloseTo(64.05, Offset.offset(0.01D));
				});
	}
}
