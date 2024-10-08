package eu.solven.kumite.board;

import java.util.Map;
import java.util.UUID;
import java.util.random.RandomGenerator;

import org.assertj.core.api.Assertions;
import org.greenrobot.eventbus.EventBus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import eu.solven.kumite.account.fake_player.RandomPlayer;
import eu.solven.kumite.account.login.IKumiteTestConstants;
import eu.solven.kumite.app.IKumiteSpringProfiles;
import eu.solven.kumite.app.KumiteServerComponentsConfiguration;
import eu.solven.kumite.contest.Contest;
import eu.solven.kumite.contest.ContestCreationMetadata;
import eu.solven.kumite.contest.ContestsRegistry;
import eu.solven.kumite.events.BoardIsUpdated;
import eu.solven.kumite.events.ContestIsGameover;
import eu.solven.kumite.exception.UnknownContestException;
import eu.solven.kumite.game.GamesRegistry;
import eu.solven.kumite.game.IGame;
import eu.solven.kumite.game.optimization.tsp.TravellingSalesmanProblem;
import eu.solven.kumite.move.IKumiteMove;
import eu.solven.kumite.move.PlayerMoveRaw;
import eu.solven.kumite.player.ContestPlayersRegistry;
import eu.solven.kumite.player.IAccountPlayersRegistry;
import eu.solven.kumite.player.KumitePlayer;
import eu.solven.kumite.player.PlayerJoinRaw;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { KumiteServerComponentsConfiguration.class })
@ActiveProfiles({ IKumiteSpringProfiles.P_INMEMORY })
public class TestBoardLifecycleManager implements IKumiteTestConstants {

	@Autowired
	RandomGenerator randomGenerator;

	@Autowired
	BoardLifecycleManager boardLifecycleManager;
	@Autowired
	BoardMutator boardMutator;
	@Autowired
	IAccountPlayersRegistry accountPlayers;
	@Autowired
	ContestPlayersRegistry contestsPlayers;
	@Autowired
	GamesRegistry gamesRegistry;
	@Autowired
	ContestsRegistry contestsRegistry;
	@Autowired
	BoardsRegistry boardsRegistry;
	@Autowired
	EventBus eventBus;

	@Test
	public void testAsync_Exception() {
		ICanGameover canGameover = Mockito.mock(ICanGameover.class);

		Assertions.assertThatThrownBy(() -> {
			boardMutator.executeBoardChange(someContestId, board -> {
				throw new Error("Any error");
			}, canGameover);
		}).isInstanceOf(UnknownContestException.class);

		Mockito.verify(canGameover, Mockito.never()).doGameover(Mockito.any(UUID.class), Mockito.anyBoolean());
	}

	IGame game = new TravellingSalesmanProblem();

	UUID accountId = UUID.randomUUID();
	UUID playerId;

	UUID contestId;
	Contest contest;

	@BeforeEach
	public void initContest() {
		if (0 == gamesRegistry.getGames().count()) {
			gamesRegistry.registerGame(game);
		}

		String contestName = "Auto-generated " + randomGenerator.nextInt(128);
		ContestCreationMetadata constantMetadata = ContestCreationMetadata.fromGame(game.getGameMetadata())
				.name(contestName)
				.author(RandomPlayer.ACCOUNT_ID)
				.build();
		IKumiteBoard initialBoard = game.generateInitialBoard(randomGenerator);
		contest = contestsRegistry.registerContest(game, constantMetadata, initialBoard);

		contestId = contest.getContestId();

		playerId = accountPlayers.generateMainPlayerId(accountId);
	}

	@Test
	public void testPlayerMove_PlayerNotRegistered() {
		accountPlayers.registerPlayer(KumitePlayer.builder().playerId(playerId).accountId(accountId).build());

		PlayerMoveRaw playerMove = makeValidMove();

		Assertions.assertThatThrownBy(() -> {
			boardLifecycleManager.onPlayerMove(contest, playerMove);
		}).isInstanceOf(IllegalArgumentException.class);
	}

	private PlayerMoveRaw makeValidMove() {
		Map<String, IKumiteMove> exampleMoves =
				game.exampleMoves(randomGenerator, contest.getBoard().get().asView(playerId), playerId);
		return PlayerMoveRaw.builder().playerId(playerId).move(exampleMoves.values().iterator().next()).build();
	}

	@Test
	public void testPlayerMove() {
		accountPlayers.registerPlayer(KumitePlayer.builder().playerId(playerId).accountId(accountId).build());

		boardLifecycleManager.registerPlayer(contest,
				PlayerJoinRaw.builder().contestId(someContestId).playerId(playerId).isViewer(false).build());

		PlayerMoveRaw playerMove = makeValidMove();
		boardLifecycleManager.onPlayerMove(contest, playerMove);

		Assertions.assertThat(boardsRegistry.hasGameover(game, contestId).isGameOver()).isFalse();
	}

	@Test
	public void testViewerMove() {
		accountPlayers.registerPlayer(KumitePlayer.builder().playerId(playerId).accountId(accountId).build());

		boardLifecycleManager.registerPlayer(contest,
				PlayerJoinRaw.builder().contestId(someContestId).playerId(playerId).isViewer(true).build());

		PlayerMoveRaw playerMove = makeValidMove();

		Assertions.assertThatThrownBy(() -> {
			boardLifecycleManager.onPlayerMove(contest, playerMove);
		}).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void testForceGameover() {
		Assertions.assertThat(boardsRegistry.hasGameover(game, contestId).isGameOver()).isFalse();

		boardLifecycleManager.forceGameOver(contest);

		Assertions.assertThat(boardsRegistry.hasGameover(game, contestId).isGameOver()).isTrue();
	}

	@Test
	public void testOnException() {
		Assertions.assertThat(boardsRegistry.hasGameover(game, contestId).isGameOver()).isFalse();

		EventSink eventSink = EventSink.makeEventSink(eventBus);

		// ICanGameover canGameover = Mockito.mock(ICanGameover.class);
		// UUID gameOverBoardStateId = UUID.randomUUID();
		// Mockito.when(canGameover.doGameover(Mockito.eq(contestId),
		// Mockito.eq(true))).thenReturn(gameOverBoardStateId);

		Assertions.assertThatThrownBy(() -> boardMutator.executeBoardChange(contestId, board -> {
			throw new IllegalArgumentException("Something went wrong");
		}, boardLifecycleManager::doGameover)).isInstanceOf(IllegalStateException.class);

		Assertions.assertThat(eventSink.getEvents())
				// As we mocked ICanGameover, we do not receive the gameOver event
				.contains(ContestIsGameover.builder().contestId(contestId).build())
				.anyMatch(e -> {
					if (e instanceof BoardIsUpdated isUpdated) {
						return isUpdated.getContestId().equals(contestId);
					} else {
						return false;
					}

				})
				.hasSize(2);
		Assertions.assertThat(boardsRegistry.hasGameover(game, contestId).isGameOver()).isTrue();

		// Mockito.verify(canGameover).doGameover(Mockito.any(UUID.class), Mockito.anyBoolean());
	}
}
