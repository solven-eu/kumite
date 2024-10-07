package eu.solven.kumite.board;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

import eu.solven.kumite.contest.Contest;
import eu.solven.kumite.contest.IHasGameover;
import eu.solven.kumite.events.BoardIsUpdated;
import eu.solven.kumite.events.ContestIsGameover;
import eu.solven.kumite.events.PlayerCanMove;
import eu.solven.kumite.events.PlayerJoinedBoard;
import eu.solven.kumite.events.PlayerMoved;
import eu.solven.kumite.move.IKumiteMove;
import eu.solven.kumite.move.INoOpKumiteMove;
import eu.solven.kumite.move.PlayerMoveRaw;
import eu.solven.kumite.player.PlayerJoinRaw;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class BoardLifecycleManager implements IBoardLifecycleManager {
	protected final BoardLifecycleManagerHelper helper;

	// This guarantees each change to a board in single-threaded
	final Executor boardEvolutionExecutor;

	/**
	 * When this returns, the caller is guaranteed its change has been executed
	 * 
	 * @param contestId
	 * @param runnableToNewStateId
	 * @return
	 */
	protected BoardSnapshotPostEvent executeBoardChange(UUID contestId,
			Function<IKumiteBoard, UUID> runnableToNewStateId) {
		// the {@link CountDownLatch} counted-down in the boardEvolutionThread
		CountDownLatch cdl = new CountDownLatch(1);
		AtomicReference<Throwable> refT = new AtomicReference<>();
		AtomicReference<BoardSnapshotPostEvent> refBoard = new AtomicReference<>();

		Contest contest = helper.getContestsRegistry().getContest(contestId);
		IHasGameover hasGameover = contest.getGameover();
		boolean wasGameover = hasGameover.isGameOver();
		UUID boardStateIdBefore = contest.getBoardMetadata().get().getBoardStateId();

		AtomicBoolean setToGameOver = new AtomicBoolean();

		log.trace("Submitting task for contestId={}", contestId);
		// How to back-pressure?
		getExecutor(contestId).execute(() -> {
			try {
				BoardSnapshotPostEvent snapshot = executeBoardMutation(contestId, runnableToNewStateId);

				refBoard.set(snapshot);
			} catch (Throwable t) {
				refT.compareAndSet(null, new IllegalStateException("Propagating", t));

				log.warn("Exception ({} - {}) while executing boardMutation.",
						t.getClass().getSimpleName(),
						t.getMessage());

				try {
					UUID boardStateId = doGameover(contestId, true);
					refBoard.set(BoardSnapshotPostEvent.builder()
							.boardStateId(boardStateId)
							.board(contest.getBoard().get())
							.enabledPlayerIds(Set.of())
							.build());
				} catch (Throwable tt) {
					// t is not suppressed, through we want to couple it with this other Throwable
					tt.addSuppressed(t);
					log.error("Error on forceGameOver triggered by Throwable", tt);
				}
			} finally {
				if (!wasGameover && hasGameover.isGameOver()) {
					// This check is done from within the boardEvolutionThread
					setToGameOver.set(true);
				}

				// countDown is the very last operation
				cdl.countDown();
				log.trace("Counted-down task for contestId={}", contestId);
			}
		});
		log.trace("Submitted task for contestId={}", contestId);

		AtomicReference<InterruptedException> refInterrupted = new AtomicReference<>();

		// `await` is the very first operation to resume the boardEvolutionThread
		boolean awaitSuccess;
		try {
			if (cdl.getCount() == 0) {
				// Skip the thread-interrupted check
				awaitSuccess = true;
			} else {
				// We give at most 1 minute for a board to execute a request change
				awaitSuccess = cdl.await(1, TimeUnit.MINUTES);
				log.trace("Awaited task for contestId={}", contestId);
			}
		} catch (InterruptedException e) {
			// Set back the interrupted flag right away
			Thread.currentThread().interrupt();

			refInterrupted.set(e);
			awaitSuccess = false;
		}

		boolean needGameover;

		if (refInterrupted.get() != null) {
			if (setToGameOver.get()) {
				log.warn("Interrupted but already gameOver contestId={}", contestId);
				needGameover = false;
			} else {
				// BEWARE: Closing the contests-server should not forceOver all contests
				log.warn("force gameOver for contestId={} as EventProcessing was interrupted", contestId);
				needGameover = true;
			}
		} else if (!awaitSuccess) {
			if (setToGameOver.get()) {
				log.warn("EventProcessing is too slow but already gameOver for contestId={}", contestId);
				needGameover = false;
			} else {
				log.warn("EventProcessing is too slow so forceGameOver for contestId={}", contestId);
				needGameover = true;
			}
		} else {
			needGameover = false;
		}

		if (needGameover) {
			// TODO This will not work as it is done out of the boardEvolutionThread
			// However, the boardEvolutionThread may not be available (due to slowness)
			helper.getBoardRegistry().registerGameover(contestId, true);

			setToGameOver.set(true);
		}

		UUID boardStateIdAfter;
		if (refBoard.get() != null) {
			BoardSnapshotPostEvent board = refBoard.get();
			boardStateIdAfter = board.getBoardStateId();

			board.getEnabledPlayerIds().forEach(enabledPlayerId -> {
				helper.getEventBus()
						.post(PlayerCanMove.builder().contestId(contestId).playerId(enabledPlayerId).build());
			});
		} else {
			boardStateIdAfter = contest.getBoardMetadata().get().getBoardStateId();
			// This would happen only when a forcegameOver fails after a mutation failure
			log.warn("We have no boardRef. We make an optimistic guess about boardStateId={}", boardStateIdAfter);
		}

		// The normal flow of events
		if (!boardStateIdBefore.equals(boardStateIdAfter)) {
			BoardIsUpdated event =
					BoardIsUpdated.builder().contestId(contestId).boardStateId(boardStateIdAfter).build();
			helper.getEventBus().post(event);
		}

		// Send gameOver event even in case of issues
		// This can not be done before `await` as `setToGameOver` would not be ready
		if (setToGameOver.get()) {
			log.info("boardEvolutionExecutor turned contestId={} into gameOver", contestId);

			// Events are sent out of the boardEvolutionThread
			helper.getEventBus().post(ContestIsGameover.builder().contestId(contestId).build());
		}

		// We prefer to return some functional exception before reporting some slowness
		Throwable t = refT.get();
		if (t != null) {
			throw new IllegalStateException("Issue processing move", t);
		} else if (refBoard.get() != null) {
			// We may have been interrupted. Though, as long as we have a refBoard, we return it considering we
			// successfully turned into gameOver
			return refBoard.get();
		} else if (refInterrupted.get() != null) {
			throw new IllegalStateException("boardEvolutionThread has been interrupted",
					new IllegalStateException(refInterrupted.get()));
		} else if (!awaitSuccess) {
			throw new IllegalStateException("EventProcessing is too slow");
		} else {
			throw new IllegalStateException("No board successfully prepared");
		}
	}

	private BoardSnapshotPostEvent executeBoardMutation(UUID contestId,
			Function<IKumiteBoard, UUID> runnableToNewStateId) {
		IHasBoard hasBoard = helper.getBoardRegistry().hasBoard(contestId);
		IKumiteBoard board = hasBoard.get();
		Set<UUID> playerCanMoveBefore = playersCanMove(contestId, board);

		log.trace("Runnning task for contestId={}", contestId);
		UUID newStateId = runnableToNewStateId.apply(board);

		Set<UUID> enabledPlayersIds = new HashSet<>();
		{
			Set<UUID> playerCanMoveAfter = playersCanMove(contestId, board);

			// This does an intersection: players turned movable can now move, through they could not move
			// before
			enabledPlayersIds.addAll(playerCanMoveAfter);
			enabledPlayersIds.removeAll(playerCanMoveBefore);
		}

		log.trace("Runnned task for contestId={}", contestId);
		return BoardSnapshotPostEvent.builder()
				.boardStateId(newStateId)
				.board(board)
				.enabledPlayerIds(enabledPlayersIds)
				.build();
	}

	/**
	 * 
	 * @param contestId
	 * @return the executor dedicated for given contestId
	 */
	private Executor getExecutor(UUID contestId) {
		return boardEvolutionExecutor;
	}

	/**
	 * 
	 * @param contest
	 * @param playerRegistrationRaw
	 * @return
	 */
	@Override
	public IKumiteBoardViewWrapper registerPlayer(Contest contest, PlayerJoinRaw playerRegistrationRaw) {
		UUID contestId = contest.getContestId();
		UUID playerId = playerRegistrationRaw.getPlayerId();

		if (helper.isGameover(contestId)) {
			// Leave before entering the executor if possible
			throw new IllegalStateException("The game is over");
		}

		BoardSnapshotPostEvent boardSnapshot = executeBoardChange(contestId, board -> {
			if (helper.isGameover(contestId)) {
				// Check again for gameOver as it may have been registered since previous check was out of the
				// boardEvolutionThread
				throw new IllegalStateException("The game is over");
			}

			// contestPlayersRegistry takes in charge the record in the board in IBoardRepository
			Optional<UUID> optBoardStateId =
					helper.getContestPlayersRegistry().registerPlayer(contest, playerRegistrationRaw);

			// may be empty in case of viewer
			return optBoardStateId
					.orElseGet(() -> helper.getBoardRegistry().getMetadata(contestId).get().getBoardStateId());
		});

		IKumiteBoardView boardViewPostMove = boardSnapshot.getBoard().asView(playerId);

		// We submit the event out of threadPool.
		// Hence we are guaranteed the event is fully processed.
		// The event subscriber can process it synchronously (through beware of deep-stack in case of long event-chains)
		// Hence we do not guarantee other events interleaved when the event is processed
		helper.getEventBus().post(PlayerJoinedBoard.builder().contestId(contestId).playerId(playerId).build());

		return KumiteBoardViewWrapper.builder()
				.boardStateId(boardSnapshot.getBoardStateId())
				.view(boardViewPostMove)
				.build();
	}

	private Set<UUID> playersCanMove(UUID contestId, IKumiteBoard board) {
		Contest contest = helper.getContestsRegistry().getContest(contestId);

		Set<UUID> movablePlayerIds = board.snapshotContenders()
				.stream()
				.filter(playerId -> canPlay(
						contest.getGame().exampleMoves(helper.getRandomGenerator(), board.asView(playerId), playerId)))
				.collect(Collectors.toSet());

		return movablePlayerIds;
	}

	private boolean canPlay(Map<String, IKumiteMove> exampleMoves) {
		return exampleMoves.values().stream().anyMatch(move -> !(move instanceof INoOpKumiteMove));
	}

	protected void precheckOnPlayerMove(UUID contestId, UUID playerId) {
		if (helper.isGameover(contestId)) {
			// Leave before entering the executor if possible
			throw new IllegalStateException("The game is over");
		}

		if (!helper.getContestPlayersRegistry().isRegisteredPlayer(contestId, playerId)) {
			List<UUID> contestPlayers = helper.getContestPlayersRegistry()
					.makeDynamicHasPlayers(contestId)
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
	}

	@Override
	public IKumiteBoardViewWrapper onPlayerMove(Contest contest, PlayerMoveRaw playerMove) {
		UUID contestId = contest.getContestId();
		UUID playerId = playerMove.getPlayerId();

		// Precheck can be done eagerly
		precheckOnPlayerMove(contestId, playerId);

		BoardSnapshotPostEvent boardSnapshot = executeBoardChange(contestId, currentBoard -> {
			// Precheck can be ensured in boardEvolutionThead
			precheckOnPlayerMove(contestId, playerId);

			// First `.checkMove`: these are generic checks (e.g. is the gamerOver?)
			try {
				contest.checkValidMove(playerMove);
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("Issue on contest=" + contest + " for move=" + playerMove, e);
			}

			log.info("Registering move for contestId={} by playerId={}", contestId, playerId);

			// This may still fail (e.g. the move is illegal given game rules)
			currentBoard.registerMove(playerMove);

			saveUpdatedBoard(contestId, currentBoard);
			return helper.getBoardRegistry().registerPlayerMoved(contestId, playerId);
		});

		IKumiteBoard boardAfter = boardSnapshot.getBoard();

		helper.getEventBus().post(PlayerMoved.builder().contestId(contestId).playerId(playerId).build());

		return KumiteBoardViewWrapper.builder()
				.view(boardAfter.asView(playerId))
				.boardStateId(boardSnapshot.getBoardStateId())
				.build();
	}

	/**
	 * This has to be called from the boardEvolutionThread
	 * 
	 * @param contestId
	 * @param board
	 */
	protected void saveUpdatedBoard(UUID contestId, IKumiteBoard board) {
		// Persist the board (e.g. for concurrent changes)
		helper.getBoardRegistry().updateBoard(contestId, board);

		if (helper.isGameover(contestId)) {
			doGameover(contestId, false);
		}
	}

	protected UUID doGameover(UUID contestId, boolean force) {
		// Mark gameOver while inside the loop. It will prevent other interactions
		log.info("doGameOver for contestId={} force={}", contestId, force);
		return helper.doGameOver(contestId, force);
	}

	@Override
	public UUID forceGameOver(Contest contest) {
		UUID contestId = contest.getContestId();

		IHasBoardMetadata hasMetadata = helper.getBoardRegistry().getMetadata(contestId);
		if (helper.isGameover(contestId)) {
			log.info("contestId={} is already gameOver", contestId);
			return hasMetadata.get().getBoardStateId();
		}

		BoardSnapshotPostEvent snapshot = executeBoardChange(contestId, board -> {
			if (helper.isGameover(contestId)) {
				log.info("contestId={} is already gameOver", contestId);
				return hasMetadata.get().getBoardStateId();
			}

			return doGameover(contestId, true);
		});

		// Gameover event is managed by `executeBoardChange`

		return snapshot.getBoardStateId();
	}

}
