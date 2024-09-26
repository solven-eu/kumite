package eu.solven.kumite.scenario;

import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.random.RandomGenerator;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import eu.solven.kumite.board.BoardLifecycleManager;
import eu.solven.kumite.board.BoardsRegistry;
import eu.solven.kumite.board.IHasBoard;
import eu.solven.kumite.board.IKumiteBoard;
import eu.solven.kumite.board.persistence.IBoardRepository;
import eu.solven.kumite.board.persistence.InMemoryBoardRepository;
import eu.solven.kumite.contest.Contest;
import eu.solven.kumite.contest.ContestCreationMetadata;
import eu.solven.kumite.game.GamesRegistry;
import eu.solven.kumite.game.IGame;
import eu.solven.kumite.game.optimization.tsp.TravellingSalesmanProblem;
import eu.solven.kumite.move.IKumiteMove;
import eu.solven.kumite.move.PlayerMoveRaw;
import eu.solven.kumite.player.ContendersFromBoard;
import eu.solven.kumite.player.ContestPlayersRegistry;
import eu.solven.kumite.player.IAccountPlayersRegistry;
import eu.solven.kumite.player.IHasPlayers;
import eu.solven.kumite.player.InMemoryViewingAccountsRepository;
import eu.solven.kumite.player.KumitePlayer;
import eu.solven.kumite.player.PlayerJoinRaw;
import eu.solven.kumite.player.persistence.BijectiveAccountPlayersRegistry;

public class TestBoardLifecycleManager {
	// We check with as async Executor to ensure proper errorManagement
	Executor executor = Executors.newSingleThreadExecutor();

	IBoardRepository boardRepository = new InMemoryBoardRepository();
	BoardsRegistry boardRegistry = new BoardsRegistry(boardRepository);
	GamesRegistry gamesRegistry = new GamesRegistry();
	IAccountPlayersRegistry accountPlayers = new BijectiveAccountPlayersRegistry();

	ContestPlayersRegistry contestPlayersRegistry = new ContestPlayersRegistry(gamesRegistry,
			accountPlayers,
			new ContendersFromBoard(accountPlayers, boardRepository),
			new InMemoryViewingAccountsRepository());

	BoardLifecycleManager boardLifecycleManager =
			new BoardLifecycleManager(boardRegistry, contestPlayersRegistry, executor);

	RandomGenerator randomGenerator = new Random(0);

	UUID contestId = UUID.randomUUID();

	@Test
	public void testAsync_Exception() {
		Assertions.assertThatThrownBy(() -> {
			boardLifecycleManager.executeBoardChange(contestId, () -> {
				throw new Error("Any error");
			});
		}).isInstanceOf(IllegalArgumentException.class);
	}

	IGame game = new TravellingSalesmanProblem();

	UUID accountId = UUID.randomUUID();
	UUID playerId = accountPlayers.generateMainPlayerId(accountId);

	IKumiteBoard board = game.generateInitialBoard(new Random(0));
	IHasBoard hasBoard;
	IHasPlayers hasPlayers;

	Contest contest;

	{
		gamesRegistry.registerGame(game);
		boardRegistry.registerBoard(contestId, board);

		hasBoard = boardRegistry.makeDynamicBoardHolder(contestId);
		hasPlayers = contestPlayersRegistry.makeDynamicHasPlayers(contestId);

		contest = Contest.builder()
				.contestId(contestId)
				.game(game)
				.players(hasPlayers)
				.board(hasBoard)
				.gameover(game.makeDynamicGameover(hasBoard))
				.constantMetadata(ContestCreationMetadata.fromGame(game.getGameMetadata())
						.name("someContestName")
						.author(accountId)
						.build())
				.build();
	}

	@Test
	public void testPlayerMove_PlayerNotRegistered() {
		accountPlayers.registerPlayer(KumitePlayer.builder().playerId(playerId).accountId(accountId).build());

		// Skip `playersRegistry.registerPlayer`

		PlayerMoveRaw playerMove = makeValidMove();

		Assertions.assertThatThrownBy(() -> {
			boardLifecycleManager.onPlayerMove(contest, playerMove);
		}).isInstanceOf(IllegalArgumentException.class);
	}

	private PlayerMoveRaw makeValidMove() {
		Map<String, IKumiteMove> exampleMoves = game.exampleMoves(randomGenerator, board.asView(playerId), playerId);
		return PlayerMoveRaw.builder().playerId(playerId).move(exampleMoves.values().iterator().next()).build();
	}

	@Test
	public void testPlayerMove() {
		accountPlayers.registerPlayer(KumitePlayer.builder().playerId(playerId).accountId(accountId).build());

		boardLifecycleManager.registerPlayer(contest,
				PlayerJoinRaw.builder().contestId(contestId).playerId(playerId).isViewer(false).build());

		PlayerMoveRaw playerMove = makeValidMove();
		boardLifecycleManager.onPlayerMove(contest, playerMove);
	}

	@Test
	public void testViewerMove() {
		accountPlayers.registerPlayer(KumitePlayer.builder().playerId(playerId).accountId(accountId).build());

		boardLifecycleManager.registerPlayer(contest,
				PlayerJoinRaw.builder().contestId(contestId).playerId(playerId).isViewer(true).build());

		PlayerMoveRaw playerMove = makeValidMove();

		Assertions.assertThatThrownBy(() -> {
			boardLifecycleManager.onPlayerMove(contest, playerMove);
		}).isInstanceOf(IllegalArgumentException.class);
	}
}
