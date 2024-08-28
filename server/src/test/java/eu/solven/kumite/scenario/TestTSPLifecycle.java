package eu.solven.kumite.scenario;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import eu.solven.kumite.account.AccountsStore;
import eu.solven.kumite.account.KumiteAccount;
import eu.solven.kumite.app.IKumiteSpringProfiles;
import eu.solven.kumite.app.KumiteServerComponentsConfiguration;
import eu.solven.kumite.contest.Contest;
import eu.solven.kumite.contest.ContestLifecycleManager;
import eu.solven.kumite.contest.ContestMetadata;
import eu.solven.kumite.contest.ContestSearchParameters;
import eu.solven.kumite.contest.ContestsStore;
import eu.solven.kumite.game.GameMetadata;
import eu.solven.kumite.game.GameSearchParameters;
import eu.solven.kumite.game.GamesStore;
import eu.solven.kumite.game.optimization.TSPBoard;
import eu.solven.kumite.game.optimization.TSPSolution;
import eu.solven.kumite.leaderboard.LeaderBoardRaw;
import eu.solven.kumite.leaderboard.LeaderboardRegistry;
import eu.solven.kumite.leaderboard.LeaderboardSearchParameters;
import eu.solven.kumite.leaderboard.PlayerDoubleScore;
import eu.solven.kumite.player.ContestPlayersRegistry;
import eu.solven.kumite.player.KumitePlayer;
import eu.solven.kumite.player.PlayerMove;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { KumiteServerComponentsConfiguration.class })
@ActiveProfiles(IKumiteSpringProfiles.P_INJECT_DEFAULT_GAMES)
public class TestTSPLifecycle {
	@Autowired
	ContestLifecycleManager lifecycleManager;

	@Autowired
	ContestPlayersRegistry contestPlayersRegistry;

	@Autowired
	AccountsStore accountsStore;

	@Autowired
	GamesStore gamesStore;

	@Autowired
	ContestsStore contestsStore;

	@Autowired
	LeaderboardRegistry leaderboardRegistry;

	@Test
	public void testSinglePlayer() {
		UUID accountId = UUID.randomUUID();
		KumiteAccount account = accountsStore
				.registerAccount(KumiteAccount.builder().accountId(accountId).playerId(UUID.randomUUID()).build());

		List<GameMetadata> games = gamesStore
				.searchGames(GameSearchParameters.builder().titlePattern(Optional.of(".*Salesman.*")).build());

		Assertions.assertThat(games).hasSize(1);

		List<ContestMetadata> contests = contestsStore.searchContests(
				ContestSearchParameters.builder().gameId(Optional.of(games.get(0).getGameId())).build());

		Assertions.assertThat(contests)
				.hasSize(1)
				.element(0)
				.matches(c -> c.isAcceptPlayers())
				.matches(c -> !c.isGameOver());

		Contest contest = contestsStore.getContest(contests.get(0).getContestId());

		KumitePlayer player = accountsStore.getAccountMainPlayer(accountId);
		Assertions.assertThat(player.getPlayerId()).isEqualTo(account.getPlayerId());

		contestPlayersRegistry.registerPlayer(contest.getContestMetadata(), player);

		List<String> orderedCities;
		{
			TSPBoard tspBoard = (TSPBoard) contest.getRefBoard().getBoard();

			orderedCities = tspBoard.getCities().stream().map(c -> c.getName()).collect(Collectors.toList());
		}
		TSPSolution rawMove = TSPSolution.builder().cities(orderedCities).build();

		PlayerMove playerMove = PlayerMove.builder()
				.contestId(contest.getContestMetadata().getContestId())
				.playerId(player.getPlayerId())
				.move(rawMove)
				.build();
		lifecycleManager.onPlayerMove(contest, playerMove);

		LeaderBoardRaw leaderboard = leaderboardRegistry.searchLeaderboard(
				LeaderboardSearchParameters.builder().contestId(contest.getContestMetadata().getContestId()).build());

		Assertions.assertThat(leaderboard.getPlayerScores()).hasSize(1);
		Assertions.assertThat(leaderboard.getPlayerScores()).element(0).matches(ps -> {
			Assertions.assertThat(ps.getPlayerId()).isEqualTo(player.getPlayerId());

			PlayerDoubleScore pds = (PlayerDoubleScore) ps;
			Assertions.assertThat(pds.getScore()).isBetween(1.0, 2.0);

			return true;
		});
	}
}
