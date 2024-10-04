package eu.solven.kumite.scenario;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import eu.solven.kumite.app.IKumiteSpringProfiles;
import eu.solven.kumite.app.KumiteServerComponentsConfiguration;
import eu.solven.kumite.board.BoardsRegistry;
import eu.solven.kumite.board.IHasBoard;
import eu.solven.kumite.contest.ActiveContestGenerator;
import eu.solven.kumite.contest.IHasGameover;
import eu.solven.kumite.game.GameSearchParameters;
import eu.solven.kumite.game.GamesRegistry;
import eu.solven.kumite.game.opposition.tictactoe.TicTacToe;
import eu.solven.kumite.leaderboard.Leaderboard;
import eu.solven.kumite.randomgamer.GamerLogicHelper;
import eu.solven.kumite.randomgamer.RandomGamer;
import eu.solven.kumite.randomgamer.RandomPlayersVsThemselves;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { KumiteServerComponentsConfiguration.class,
		ActiveContestGenerator.class,
		RandomGamer.class,
		GamerLogicHelper.class, })
@ActiveProfiles({ IKumiteSpringProfiles.P_INMEMORY })
public class TestTicTacToe {

	@Autowired
	GamesRegistry gamesRegistry;

	@Autowired
	BoardsRegistry boardsRegistry;

	@Autowired
	ActiveContestGenerator activeContestGenerator;

	@Autowired
	RandomGamer randomGamer;

	TicTacToe game = new TicTacToe();

	@BeforeEach
	public void injectGame() {
		gamesRegistry.registerGame(game);
	}

	@Test
	public void testGame() throws JsonMappingException, JsonProcessingException {
		Assertions.assertThat(activeContestGenerator.makeContestsIfNoneJoinable()).isEqualTo(1);

		Map<UUID, Set<UUID>> contestIds = randomGamer.joinOncePerContestAndPlayer(
				GameSearchParameters.byGameId(game.getGameMetadata().getGameId()),
				new RandomPlayersVsThemselves());
		Assertions.assertThat(contestIds).hasSize(1);
		UUID contestId = contestIds.keySet().iterator().next();
		Set<UUID> playerIds = contestIds.get(contestId);
		Assertions.assertThat(playerIds).hasSize(2);
		UUID playerId1 = playerIds.stream().skip(0).findFirst().get();
		UUID playerId2 = playerIds.stream().skip(1).findFirst().get();

		int totalMoves = 0;
		do {
			int additionalMoves = randomGamer.playOncePerContestAndPlayer();

			if (additionalMoves == 0) {
				break;
			}

			totalMoves += additionalMoves;
		} while (true);

		// We need at least 5 moves to win (3 Xs and 2 Os)
		// Until 9 moves for a tie
		Assertions.assertThat(totalMoves).isBetween(5, 9);

		IHasBoard hasBoard = boardsRegistry.makeDynamicBoardHolder(contestId);

		IHasGameover hasGameover = boardsRegistry.hasGameover(game, contestId);

		// There must be a winner after having played at most 5 pair of moves
		Assertions.assertThat(hasGameover.isGameOver()).isTrue();

		Leaderboard leaderboard = game.makeLeaderboard(hasBoard.get());
		Assertions.assertThat(leaderboard.getPlayerIdToPlayerScore()).hasSize(2);
		Assertions.assertThat(leaderboard.getPlayerIdToPlayerScore().keySet())
				.containsExactlyInAnyOrder(playerId1, playerId2);

		// if (totalMoves % 2 == 0) {
		// The first movingPlayer (X) is the winner
		// hasBoard.get().
		// }
	}
}
