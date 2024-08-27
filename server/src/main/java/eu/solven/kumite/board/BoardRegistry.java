package eu.solven.kumite.board;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BoardRegistry {
	Map<UUID, Board> contestIdToBoard = new ConcurrentHashMap<>();

	// This guarantees each change to a board in single-threaded
	Executor boardEvolutionExecutor;

	public void registerBoard(UUID contestId, Board initialBoard) {
		Board alreadyIn = contestIdToBoard.putIfAbsent(contestId, initialBoard);
		if (alreadyIn != null) {
			throw new IllegalArgumentException("contestId already registered: " + alreadyIn);
		}
	}

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
}
