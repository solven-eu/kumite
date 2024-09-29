package eu.solven.kumite.scenario;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
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
import eu.solven.kumite.game.GameSearchParameters;
import eu.solven.kumite.game.GamesRegistry;
import eu.solven.kumite.game.IGameMetadataConstants;
import eu.solven.kumite.game.opposition.tictactoe.TicTacToe;
import eu.solven.kumite.game.optimization.tsp.TravellingSalesmanProblem;
import eu.solven.kumite.leaderboard.Leaderboard;
import eu.solven.kumite.randomgamer.RandomGamer;
import eu.solven.kumite.randomgamer.RandomPlayersVsThemselves;

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
		RandomGamer.class,

		RandomPlayersVsThemselves.class,

})
@ActiveProfiles({ IKumiteSpringProfiles.P_INMEMORY })
public class TestTwoGames_Manual {

	@Autowired
	GamesRegistry gamesRegistry;

	@Autowired
	ContestsRegistry contestsRegistry;

	@Autowired
	BoardsRegistry boardsRegistry;

	@Autowired
	ActiveContestGenerator activeContestGenerator;

	@Autowired
	RandomGamer randomGamer;

	TicTacToe game1 = new TicTacToe();
	TravellingSalesmanProblem game2 = new TravellingSalesmanProblem();

	@BeforeEach
	public void injectGame() {
		gamesRegistry.registerGame(game1);
		gamesRegistry.registerGame(game2);
	}

	@Test
	public void testGame() throws JsonMappingException, JsonProcessingException {
		int nbGames = 2;
		Assertions.assertThat(gamesRegistry.searchGames(GameSearchParameters.builder().build())).hasSize(nbGames);

		// Create playable contests:
		Assertions.assertThat(activeContestGenerator.makeContestsIfNoneJoinable()).isEqualTo(nbGames);

		int nbPlayers = Math.max(game1.getGameMetadata().getMinPlayers(), game2.getGameMetadata().getMinPlayers());
		Map<UUID, Set<UUID>> contestIds = randomGamer
				.joinOncePerContestAndPlayer(GameSearchParameters.builder().build(), new RandomPlayersVsThemselves());
		Assertions.assertThat(contestIds).hasSize(nbGames);

		for (UUID contestId : contestIds.keySet()) {
			Set<UUID> playerIds = contestIds.get(contestId);

			Assertions.assertThat(playerIds).hasSizeBetween(1, nbPlayers);
		}

		// Optimization games can be played indefinitely: we stop after given number of moves
		for (int i = 0; i < 16; i++) {
			randomGamer.playOncePerContestAndPlayer();
		}

		Stream.of(game1, game2).forEach(game -> {
			List<Contest> contests = contestsRegistry.searchContests(
					ContestSearchParameters.builder().gameId(Optional.of(game.getGameMetadata().getGameId())).build());
			Assertions.assertThat(contests).hasSize(1);
			IHasBoard hasBoard = contests.get(0).getBoard();

			IHasGameover hasGameover = game.makeDynamicGameover(hasBoard);

			if (game.getGameMetadata().getTags().contains(IGameMetadataConstants.TAG_OPTIMIZATION)) {
				// Optimization games can be played indefinitely

				Leaderboard leaderboard = game.makeLeaderboard(hasBoard.get());
				Assertions.assertThat(leaderboard.getPlayerIdToPlayerScore()).hasSize(1);
			} else {
				Assertions.assertThat(hasGameover.isGameOver()).isTrue();

				Leaderboard leaderboard = game.makeLeaderboard(hasBoard.get());
				Assertions.assertThat(leaderboard.getPlayerIdToPlayerScore()).hasSize(2);
				// Assertions.assertThat(leaderboard.getPlayerIdToPlayerScore().keySet())
				// .containsExactlyInAnyOrder(playerId1, playerId2);
			}
		});
	}
}
