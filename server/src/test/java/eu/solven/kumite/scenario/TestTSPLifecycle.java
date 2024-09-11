package eu.solven.kumite.scenario;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import eu.solven.kumite.account.KumiteUser;
import eu.solven.kumite.account.KumiteUserRaw;
import eu.solven.kumite.account.KumiteUserRawRaw;
import eu.solven.kumite.account.login.KumiteUsersRegistry;
import eu.solven.kumite.app.IKumiteSpringProfiles;
import eu.solven.kumite.app.KumiteServerComponentsConfiguration;
import eu.solven.kumite.contest.Contest;
import eu.solven.kumite.contest.ContestSearchParameters;
import eu.solven.kumite.contest.ContestsRegistry;
import eu.solven.kumite.game.GameMetadata;
import eu.solven.kumite.game.GameSearchParameters;
import eu.solven.kumite.game.GamesRegistry;
import eu.solven.kumite.game.optimization.tsp.TSPBoard;
import eu.solven.kumite.game.optimization.tsp.TSPSolution;
import eu.solven.kumite.game.optimization.tsp.TravellingSalesmanProblem;
import eu.solven.kumite.leaderboard.LeaderBoardRaw;
import eu.solven.kumite.leaderboard.LeaderboardRegistry;
import eu.solven.kumite.leaderboard.LeaderboardSearchParameters;
import eu.solven.kumite.leaderboard.PlayerDoubleScore;
import eu.solven.kumite.lifecycle.BoardLifecycleManager;
import eu.solven.kumite.lifecycle.ContestLifecycleManager;
import eu.solven.kumite.player.ContestPlayersRegistry;
import eu.solven.kumite.player.IKumiteMove;
import eu.solven.kumite.player.KumitePlayer;
import eu.solven.kumite.player.PlayerJoinRaw;
import eu.solven.kumite.player.PlayerMoveRaw;

/**
 * 
 * @author Benoit Lacelle
 * @see TestTSPLifecycleThroughRouter
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { KumiteServerComponentsConfiguration.class })
@ActiveProfiles(IKumiteSpringProfiles.P_INJECT_DEFAULT_GAMES)
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
	ContestLifecycleManager contestLifecycleManager;

	@Autowired
	BoardLifecycleManager boardLifecycleManager;

	public static final KumiteUserRaw userRaw() {
		KumiteUserRawRaw rawRaw = KumiteUserRawRaw.builder().providerId("testProviderId").sub("test").build();
		return KumiteUserRaw.builder().rawRaw(rawRaw).email("test@test").username("fakeUsername").build();
	}

	@Test
	public void testSinglePlayer() {
		KumiteUser account = usersRegistry.registerOrUpdate(userRaw());
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
		Map<String, IKumiteMove> nameToMove =
				new TravellingSalesmanProblem().exampleMoves(tspBoard.asView(player.getPlayerId()), accountId);
		TSPSolution rawMove = (TSPSolution) nameToMove.get("lexicographical");

		PlayerMoveRaw playerMove = PlayerMoveRaw.builder().playerId(player.getPlayerId()).move(rawMove).build();
		boardLifecycleManager.onPlayerMove(contest, playerMove);

		LeaderBoardRaw leaderboard = leaderboardRegistry
				.searchLeaderboard(LeaderboardSearchParameters.builder().contestId(contest.getContestId()).build());

		Assertions.assertThat(leaderboard.getPlayerScores()).hasSize(1);
		Assertions.assertThat(leaderboard.getPlayerScores())

				// lexicographical
				.anySatisfy(ps -> {
					Assertions.assertThat(ps.getPlayerId()).isEqualTo(player.getPlayerId());

					PlayerDoubleScore pds = (PlayerDoubleScore) ps;
					Assertions.assertThat(pds.getScore()).isBetween(65.79, 65.80);
				})
		// greedy
		// .anySatisfy(ps -> {
		// Assertions.assertThat(ps.getPlayerId()).isEqualTo(player.getPlayerId());
		//
		// PlayerDoubleScore pds = (PlayerDoubleScore) ps;
		// Assertions.assertThat(pds.getScore()).isBetween(10.50, 10.51);
		// })
		;
	}
}
