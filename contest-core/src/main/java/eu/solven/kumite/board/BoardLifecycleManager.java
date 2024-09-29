package eu.solven.kumite.board;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.greenrobot.eventbus.EventBus;

import eu.solven.kumite.contest.Contest;
import eu.solven.kumite.events.PlayerJoinedBoard;
import eu.solven.kumite.events.PlayerMoved;
import eu.solven.kumite.move.PlayerMoveRaw;
import eu.solven.kumite.player.ContestPlayersRegistry;
import eu.solven.kumite.player.PlayerJoinRaw;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class BoardLifecycleManager {
	final BoardsRegistry boardRegistry;

	final ContestPlayersRegistry contestPlayersRegistry;

	// This guarantees each change to a board in single-threaded
	final Executor boardEvolutionExecutor;

	final EventBus eventBus;

	/**
	 * When this returns, the caller is guaranteed its change has been executed
	 * 
	 * @param contestId
	 * @param runnable
	 */
	public void executeBoardChange(UUID contestId, Runnable runnable) {
		if (isDirect(boardEvolutionExecutor)) {
			boardEvolutionExecutor.execute(runnable);
		} else {
			CountDownLatch cdl = new CountDownLatch(1);
			AtomicReference<Throwable> refT = new AtomicReference<>();

			log.trace("Submitting task for contestId={}", contestId);
			getExecutor(contestId).execute(() -> {
				try {
					log.trace("Runnning task for contestId={}", contestId);
					runnable.run();
					log.trace("Runnned task for contestId={}", contestId);
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
		}
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

	public void registerPlayer(Contest contest, PlayerJoinRaw playerRegistrationRaw) {
		UUID contestId = contest.getContestId();
		executeBoardChange(contestId, () -> {
			// The registry takes in charge the registration in the board
			contestPlayersRegistry.registerPlayer(contest, playerRegistrationRaw);
		});

		// We submit the event out of threadPool.
		// Hence we are guaranteed the event is fully processed.
		// The event subscriber can process it synchronously (through beware of deep-stack in case of long event-chains)
		// Hence we do not guarantee other events interleaved when the event is processed
		eventBus.post(
				PlayerJoinedBoard.builder().contestId(contestId).playerId(playerRegistrationRaw.getPlayerId()).build());
	}

	public IKumiteBoardView onPlayerMove(Contest contest, PlayerMoveRaw playerMove) {
		UUID contestId = contest.getContestId();

		AtomicReference<IKumiteBoardView> refBoardView = new AtomicReference<>();

		executeBoardChange(contestId, () -> {
			UUID playerId = playerMove.getPlayerId();

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

			IKumiteBoard currentBoard = boardRegistry.makeDynamicBoardHolder(contestId).get();

			// First `.checkMove`: these are generic checks (e.g. is the gamerOver?)
			try {
				contest.checkValidMove(playerMove);
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("Issue on contest=" + contest, e);
			}

			log.info("Registering move for contestId={} by playerId={}", contestId, playerId);

			// This may still fail (e.g. the move is illegal given game rules)
			currentBoard.registerMove(playerMove);

			// Persist the board (e.g. for concurrent changes)
			boardRegistry.updateBoard(contestId, currentBoard);

			refBoardView.set(currentBoard.asView(playerId));

		});

		IKumiteBoardView boardViewPostMove = refBoardView.get();

		if (boardViewPostMove == null) {
			throw new IllegalStateException("Should have failed, or have produced a view");
		}

		eventBus.post(PlayerMoved.builder().contestId(contestId).playerId(playerMove.getPlayerId()).build());

		return boardViewPostMove;
	}
}
