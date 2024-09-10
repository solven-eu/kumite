package eu.solven.kumite.scenario;

import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import eu.solven.kumite.board.BoardsRegistry;
import eu.solven.kumite.board.IHasBoard;
import eu.solven.kumite.board.IKumiteBoard;
import eu.solven.kumite.contest.Contest;
import eu.solven.kumite.contest.ContestCreationMetadata;
import eu.solven.kumite.game.GamesRegistry;
import eu.solven.kumite.game.IGame;
import eu.solven.kumite.game.optimization.tsp.TravellingSalesmanProblem;
import eu.solven.kumite.lifecycle.BoardLifecycleManager;
import eu.solven.kumite.player.AccountPlayersRegistry;
import eu.solven.kumite.player.ContestPlayersRegistry;
import eu.solven.kumite.player.IHasPlayers;
import eu.solven.kumite.player.IKumiteMove;
import eu.solven.kumite.player.KumitePlayer;
import eu.solven.kumite.player.PlayerJoinRaw;
import eu.solven.kumite.player.PlayerMoveRaw;

public class TestBoardLifecycleManager {
	// We check with as async Executor to ensure proper errorManagement
	Executor executor = Executors.newSingleThreadExecutor();

	BoardsRegistry boardRegistry = new BoardsRegistry();
	GamesRegistry gamesRegistry = new GamesRegistry();
	AccountPlayersRegistry playersRegistry = new AccountPlayersRegistry(gamesRegistry);

	ContestPlayersRegistry contestPlayersRegistry = new ContestPlayersRegistry(gamesRegistry, playersRegistry);

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

	IGame game = new TravellingSalesmanProblem();

	IKumiteBoard board = game.generateInitialBoard(new Random(0));
	IHasBoard hasBoard = () -> board;
	IHasPlayers hasPlayers = contestPlayersRegistry.makeDynamicHasPlayers(contestId);
	Contest contest = Contest.builder()
			.contestId(contestId)
			.game(game)
			.players(hasPlayers)
			.board(hasBoard)
			.gameover(game.makeDynamicGameover(hasBoard))
			.constantMetadata(ContestCreationMetadata.fromGame(game.getGameMetadata()).name("someContestName").build())
			.build();

	UUID playerId = UUID.randomUUID();

	@Test
	public void testPlayerMove_PlayerNotRegistered() {
		gamesRegistry.registerGame(game);
		boardRegistry.registerBoard(contestId, board);

		PlayerMoveRaw playerMove = makeValidMove();

		Assertions.assertThatThrownBy(() -> {
			manager.onPlayerMove(contest, playerMove);
		}).isInstanceOf(IllegalArgumentException.class);
	}

	private PlayerMoveRaw makeValidMove() {
		Map<String, IKumiteMove> exampleMoves = game.exampleMoves(board.asView(playerId), playerId);
		return PlayerMoveRaw.builder().playerId(playerId).move(exampleMoves.values().iterator().next()).build();
	}

	@Test
	public void testPlayerMove() {
		gamesRegistry.registerGame(game);
		boardRegistry.registerBoard(contestId, board);

		playersRegistry.registerPlayer(UUID.randomUUID(), KumitePlayer.builder().playerId(playerId).build());

		manager.registerPlayer(contest,
				PlayerJoinRaw.builder().contestId(contestId).playerId(playerId).isViewer(false).build());

		PlayerMoveRaw playerMove = makeValidMove();
		manager.onPlayerMove(contest, playerMove);
	}

	@Test
	public void testViewerMove() {
		gamesRegistry.registerGame(game);
		boardRegistry.registerBoard(contestId, board);

		playersRegistry.registerPlayer(UUID.randomUUID(), KumitePlayer.builder().playerId(playerId).build());

		manager.registerPlayer(contest,
				PlayerJoinRaw.builder().contestId(contestId).playerId(playerId).isViewer(true).build());

		PlayerMoveRaw playerMove = makeValidMove();

		Assertions.assertThatThrownBy(() -> {
			manager.onPlayerMove(contest, playerMove);
		}).isInstanceOf(IllegalArgumentException.class);
	}
}
