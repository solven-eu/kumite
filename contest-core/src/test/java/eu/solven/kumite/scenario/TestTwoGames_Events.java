package eu.solven.kumite.scenario;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.greenrobot.eventbus.EventBus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import eu.solven.kumite.account.fake_player.RandomPlayer;
import eu.solven.kumite.app.IKumiteSpringProfiles;
import eu.solven.kumite.app.KumiteServerComponentsConfiguration;
import eu.solven.kumite.board.BoardsRegistry;
import eu.solven.kumite.board.IHasBoard;
import eu.solven.kumite.contest.ActiveContestGenerator;
import eu.solven.kumite.contest.Contest;
import eu.solven.kumite.contest.ContestSearchParameters;
import eu.solven.kumite.contest.ContestsRegistry;
import eu.solven.kumite.contest.IHasGameover;
import eu.solven.kumite.game.GamesRegistry;
import eu.solven.kumite.game.IGameMetadataConstants;
import eu.solven.kumite.game.opposition.tictactoe.TicTacToe;
import eu.solven.kumite.game.optimization.tsp.TravellingSalesmanProblem;
import eu.solven.kumite.leaderboard.Leaderboard;
import eu.solven.kumite.randomgamer.RandomPlaysVs1Config.RandomPlayerEventSubscriber;

/**
 * This is useful the {@link RandomPlayer} does not trying to play one game move to another game contest.
 * 
 * This does relies on {@link EventBus} to play RandomPlayer.
 * 
 * @author Benoit Lacelle
 * @see RandomPlayerEventSubscriber
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {

		KumiteServerComponentsConfiguration.class,

		// Contest generation is not done automatically, else it would trigger the whole gamePlay by RandomPlayer while
		// bootstrapping the unitTest class
		ActiveContestGenerator.class,

})
@ActiveProfiles({ IKumiteSpringProfiles.P_INMEMORY, IKumiteSpringProfiles.P_RANDOM_PLAYS_VSTHEMSELVES })
public class TestTwoGames_Events {

	@Autowired
	GamesRegistry gamesRegistry;

	@Autowired
	ContestsRegistry contestsRegistry;

	@Autowired
	BoardsRegistry boardsRegistry;

	@Autowired
	ActiveContestGenerator activeContestGenerator;

	TicTacToe game1 = new TicTacToe();
	TravellingSalesmanProblem game2 = new TravellingSalesmanProblem();

	@BeforeEach
	public void injectGame() {
		gamesRegistry.registerGame(game1);
		gamesRegistry.registerGame(game2);
	}

	@Test
	public void testGame() throws JsonMappingException, JsonProcessingException {
		// Create playable contests: it will trigger moves automatically given
		// `IKumiteSpringProfiles.P_RANDOM_PLAYS_VSTHEMSELVES`

		// When
		// This will trigger a flow of events until gameOver
		Assertions.assertThat(activeContestGenerator.makeContestsIfNoneJoinable()).isEqualTo(2);

		// Then
		Stream.of(game1, game2).forEach(game -> {
			List<Contest> contests = contestsRegistry.searchContests(
					ContestSearchParameters.builder().gameId(Optional.of(game.getGameMetadata().getGameId())).build());
			Assertions.assertThat(contests).hasSize(1);
			Contest contest = contests.get(0);
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
				Assertions.assertThat(leaderboard.getPlayerIdToPlayerScore()).hasSize(2);
			}
		});
	}
}
