package eu.solven.kumite.lifecycle;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import eu.solven.kumite.board.BoardsRegistry;
import eu.solven.kumite.board.IKumiteBoard;
import eu.solven.kumite.contest.Contest;
import eu.solven.kumite.player.ContestPlayersRegistry;
import eu.solven.kumite.player.PlayerMoveRaw;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class BoardLifecycleManager {
	final BoardsRegistry boardRegistry;

	final ContestPlayersRegistry contestPlayersRegistry;

	// This guarantees each change to a board in single-threaded
	final Executor boardEvolutionExecutor;

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

			log.trace("Submitting task for contestId={}", contestId);
			getExecutor(contestId).execute(() -> {
				try {
					log.trace("Runnning task for contestId={}", contestId);
					runnable.run();
					log.trace("Runnned task for contestId={}", contestId);
				} catch (Throwable t) {

				} finally {
					cdl.countDown();
					log.trace("Counted-down task for contestId={}", contestId);
				}
			});
			log.trace("Submitted task for contestId={}", contestId);

			try {
				cdl.await(1, TimeUnit.SECONDS);
				log.trace("Awaited task for contestId={}", contestId);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new IllegalStateException(e);
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

	public void onPlayerMove(Contest contest, PlayerMoveRaw playerMove) {
		UUID contestId = contest.getContestMetadata().getContestId();
		executeBoardChange(contestId, () -> {
			UUID playerId = playerMove.getPlayerId();

			if (!contestPlayersRegistry.isRegisteredPlayer(contestId, playerId)) {
				throw new IllegalArgumentException(
						"playerId=" + playerId + " is not registered in contestId=" + contestId);
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
		});
	}
}
