package eu.solven.kumite.scenario;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import eu.solven.kumite.board.BoardsRegistry;
import eu.solven.kumite.board.IHasBoard;
import eu.solven.kumite.contest.Contest;
import eu.solven.kumite.contest.ContestMetadata;
import eu.solven.kumite.game.GamesRegistry;
import eu.solven.kumite.game.optimization.tsp.TSPBoard;
import eu.solven.kumite.game.optimization.tsp.TravellingSalesmanProblem;
import eu.solven.kumite.lifecycle.BoardLifecycleManager;
import eu.solven.kumite.player.ContestPlayersRegistry;
import eu.solven.kumite.player.IHasPlayers;
import eu.solven.kumite.player.IKumiteMove;
import eu.solven.kumite.player.KumitePlayer;
import eu.solven.kumite.player.PlayerMoveRaw;

public class TestBoardLifecycleManager {
	// We check with as async Executor to ensure proper errorManagement
	Executor executor = Executors.newSingleThreadExecutor();

	BoardsRegistry boardRegistry = new BoardsRegistry();
	GamesRegistry gamesRegistry = new GamesRegistry();

	ContestPlayersRegistry contestPlayersRegistry = new ContestPlayersRegistry(gamesRegistry);

	BoardLifecycleManager manager = new BoardLifecycleManager(boardRegistry, contestPlayersRegistry, executor);
	UUID contestId = UUID.randomUUID();

	@Test
	public void testAsync_Exception() {
		Assertions.assertThatThrownBy(() -> {
			manager.executeBoardChange(contestId, () -> {
				throw new Error("Any error");
			});
		}).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void testPlayerNotRegistered() {
		TravellingSalesmanProblem game = new TravellingSalesmanProblem();

		TSPBoard board = game.generateInitialBoard(new Random(0));
		IHasBoard hasBoard = () -> board;
		List<KumitePlayer> players = new ArrayList<>();
		IHasPlayers hasPlayers = () -> players;
		ContestMetadata contestMetadata = ContestMetadata.builder()
				.contestId(contestId)
				.gameMetadata(game.getGameMetadata())
				.hasPlayers(hasPlayers)
				.name("someContestName")
				.build();
		Contest contest = Contest.builder()
				.board(hasBoard)
				.game(game)
				.hasPlayers(hasPlayers)
				.contestMetadata(contestMetadata)
				.build();

		UUID playerId = UUID.randomUUID();
		Map<String, IKumiteMove> exampleMoves = game.exampleMoves(board.asView(playerId), playerId);
		PlayerMoveRaw playerMove =
				PlayerMoveRaw.builder().playerId(playerId).move(exampleMoves.values().iterator().next()).build();
		manager.onPlayerMove(contest, playerMove);
	}
}
