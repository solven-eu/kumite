package eu.solven.kumite.board;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;

import org.greenrobot.eventbus.EventBus;

import eu.solven.kumite.contest.Contest;
import eu.solven.kumite.contest.ContestsRegistry;
import eu.solven.kumite.events.ContestIsGameover;
import eu.solven.kumite.events.PlayerCanMove;
import eu.solven.kumite.events.PlayerJoinedBoard;
import eu.solven.kumite.events.PlayerMoved;
import eu.solven.kumite.move.IKumiteMove;
import eu.solven.kumite.move.INoOpKumiteMove;
import eu.solven.kumite.move.PlayerMoveRaw;
import eu.solven.kumite.player.ContestPlayersRegistry;
import eu.solven.kumite.player.PlayerJoinRaw;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class BoardLifecycleManager {
	final ContestsRegistry contestsRegistry;
	final BoardsRegistry boardRegistry;

	final ContestPlayersRegistry contestPlayersRegistry;

	// This guarantees each change to a board in single-threaded
	final Executor boardEvolutionExecutor;

	final EventBus eventBus;

	final RandomGenerator randomGenerator;

	/**
	 * When this returns, the caller is guaranteed its change has been executed
	 * 
	 * @param contestId
	 * @param runnable
	 * @return
	 */
	protected BoardSnapshotPostEvent executeBoardChange(UUID contestId, Consumer<IKumiteBoard> runnable) {
		if (isDirect(boardEvolutionExecutor)) {
			return executeBoardMutation(contestId, runnable);
		} else {
			CountDownLatch cdl = new CountDownLatch(1);
			AtomicReference<Throwable> refT = new AtomicReference<>();
			AtomicReference<BoardSnapshotPostEvent> refBoard = new AtomicReference<>();

			log.trace("Submitting task for contestId={}", contestId);
			getExecutor(contestId).execute(() -> {
				try {
					BoardSnapshotPostEvent snapshot = executeBoardMutation(contestId, runnable);

					refBoard.set(snapshot);
				} catch (Throwable t) {
					refT.compareAndSet(null, t);
				} finally {
					cdl.countDown();
					log.trace("Counted-down task for contestId={}", contestId);
				}
			});
			log.trace("Submitted task for contestId={}", contestId);

			boolean awaitSuccess;
			try {
				awaitSuccess = cdl.await(15, TimeUnit.MINUTES);
				log.trace("Awaited task for contestId={}", contestId);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new IllegalStateException(e);
			}

			// We prefer to return some functional exception before reporting some slowness
			Throwable t = refT.get();
			if (t != null) {
				throw new IllegalArgumentException("Issue processing move", t);
			}

			if (!awaitSuccess) {
				// BEWARE WHAT DOES IT MEAN? THE BOARD IS CORRUPTED?
				throw new IllegalStateException("One move has been too slow to be processed");
			}

			return refBoard.get();
		}
	}

	private BoardSnapshotPostEvent executeBoardMutation(UUID contestId, Consumer<IKumiteBoard> runnable) {
		IHasBoard hasBoard = boardRegistry.makeDynamicBoardHolder(contestId);
		IKumiteBoard board = hasBoard.get();
		Set<UUID> playerCanMoveBefore = playersCanMove(contestId, board);

		log.trace("Runnning task for contestId={}", contestId);
		runnable.accept(board);

		Set<UUID> enabledPlayersIds = new HashSet<>();
		{
			Set<UUID> playerCanMoveAfter = playersCanMove(contestId, board);

			// This does an intersection: players turned movable can now move, through they could not move
			// before
			enabledPlayersIds.addAll(playerCanMoveAfter);
			enabledPlayersIds.removeAll(playerCanMoveBefore);
		}

		log.trace("Runnned task for contestId={}", contestId);
		return BoardSnapshotPostEvent.builder().board(board).enabledPlayerIds(enabledPlayersIds).build();
	}

	/**
	 * 
	 * @param contestId
	 * @return the executor dedicated for given contestId
	 */
	private Executor getExecutor(UUID contestId) {
		return boardEvolutionExecutor;
	}

	protected static boolean isDirect(Executor executor) {
		return false;
	}

	/**
	 * 
	 * @param contest
	 * @param playerRegistrationRaw
	 * @return
	 */
	public IKumiteBoardView registerPlayer(Contest contest, PlayerJoinRaw playerRegistrationRaw) {
		UUID contestId = contest.getContestId();
		UUID playerId = playerRegistrationRaw.getPlayerId();

		AtomicReference<IKumiteBoardView> refBoardView = new AtomicReference<>();

		BoardSnapshotPostEvent boardSnapshot = executeBoardChange(contestId, board -> {
			// contestPlayersRegistry takes in charge the registration in the board
			contestPlayersRegistry.registerPlayer(contest, playerRegistrationRaw);

			refBoardView.set(board.asView(playerId));
		});

		IKumiteBoardView boardViewPostMove = refBoardView.get();

		if (boardViewPostMove == null) {
			throw new IllegalStateException("Should have failed, or have produced a view");
		}

		// We submit the event out of threadPool.
		// Hence we are guaranteed the event is fully processed.
		// The event subscriber can process it synchronously (through beware of deep-stack in case of long event-chains)
		// Hence we do not guarantee other events interleaved when the event is processed
		eventBus.post(PlayerJoinedBoard.builder().contestId(contestId).playerId(playerId).build());

		boardSnapshot.getEnabledPlayerIds().forEach(enabledPlayerId -> {
			eventBus.post(PlayerCanMove.builder().contestId(contestId).playerId(playerId).build());
		});

		if (boardRegistry.hasGameover(contest.getGame(), contestId).isGameOver()) {
			eventBus.post(ContestIsGameover.builder().contestId(contestId).build());
		}

		return boardViewPostMove;
	}

	private Set<UUID> playersCanMove(UUID contestId, IKumiteBoard board) {
		Contest contest = contestsRegistry.getContest(contestId);

		Set<UUID> movablePlayerIds = board.snapshotPlayers()
				.stream()
				.filter(playerId -> canPlay(
						contest.getGame().exampleMoves(randomGenerator, board.asView(playerId), playerId)))
				.collect(Collectors.toSet());

		return movablePlayerIds;
	}

	private boolean canPlay(Map<String, IKumiteMove> exampleMoves) {
		return exampleMoves.values().stream().anyMatch(move -> !(move instanceof INoOpKumiteMove));
	}

	public IKumiteBoardView onPlayerMove(Contest contest, PlayerMoveRaw playerMove) {
		UUID contestId = contest.getContestId();
		UUID playerId = playerMove.getPlayerId();

		BoardSnapshotPostEvent boardSnapshot = executeBoardChange(contestId, currentBoard -> {
			if (!contestPlayersRegistry.isRegisteredPlayer(contestId, playerId)) {
				List<UUID> contestPlayers = contestPlayersRegistry.makeDynamicHasPlayers(contestId)
						.getPlayers()
						.stream()
						.map(p -> p.getPlayerId())
						.collect(Collectors.toList());
				throw new IllegalArgumentException("playerId=" + playerId
						+ " is not registered in contestId="
						+ contestId
						+ " Registered players: "
						+ contestPlayers);
			}

			// First `.checkMove`: these are generic checks (e.g. is the gamerOver?)
			try {
				contest.checkValidMove(playerMove);
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("Issue on contest=" + contest + " for move=" + playerMove, e);
			}

			log.info("Registering move for contestId={} by playerId={}", contestId, playerId);

			// This may still fail (e.g. the move is illegal given game rules)
			currentBoard.registerMove(playerMove);

			// Persist the board (e.g. for concurrent changes)
			boardRegistry.updateBoard(contestId, currentBoard);

			if (boardRegistry.hasGameover(contest.getGame(), contestId).isGameOver()) {
				log.info("playerMove led to gameOver for contestId={}", contestId);
				doGameOver(contestId);
			}
		});

		IKumiteBoard boardAfter = boardSnapshot.getBoard();
		IKumiteBoardView boardViewPostMove = boardAfter.asView(playerId);

		if (boardViewPostMove == null) {
			throw new IllegalStateException("Should have failed, or have produced a view");
		}

		eventBus.post(PlayerMoved.builder().contestId(contestId).playerId(playerId).build());

		boardSnapshot.getEnabledPlayerIds().forEach(enabledPlayerId -> {
			eventBus.post(PlayerCanMove.builder().contestId(contestId).playerId(playerId).build());
		});

		if (boardRegistry.hasGameover(contest.getGame(), contestId).isGameOver()) {
			eventBus.post(ContestIsGameover.builder().contestId(contestId).build());
		}

		return boardViewPostMove;
	}

	public void forceGameOver(Contest contest) {
		UUID contestId = contest.getContestId();

		if (boardRegistry.hasGameover(contest.getGame(), contestId).isGameOver()) {
			log.info("contestId={} is already gameOver", contestId);
		}

		executeBoardChange(contestId, board -> {
			log.info("Registering forcedGameOver for contestId={}", contestId);

			doGameOver(contestId);
		});

		if (boardRegistry.hasGameover(contest.getGame(), contestId).isGameOver()) {
			eventBus.post(ContestIsGameover.builder().contestId(contestId).build());
		}
	}

	private void doGameOver(UUID contestId) {
		boardRegistry.forceGameover(contestId);
		contestsRegistry.deleteContest(contestId);
		contestPlayersRegistry.forceGameover(contestId);
	}
}
