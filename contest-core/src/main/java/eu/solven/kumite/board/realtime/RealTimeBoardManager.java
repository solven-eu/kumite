package eu.solven.kumite.board.realtime;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.greenrobot.eventbus.Subscribe;
import org.springframework.beans.factory.InitializingBean;

import eu.solven.kumite.board.BoardLifecycleManager;
import eu.solven.kumite.board.BoardLifecycleManagerHelper;
import eu.solven.kumite.board.IKumiteBoard;
import eu.solven.kumite.contest.Contest;
import eu.solven.kumite.contest.ContestSearchParameters;
import eu.solven.kumite.events.ContestIsCreated;
import eu.solven.kumite.game.IGameMetadataConstants;
import lombok.extern.slf4j.Slf4j;

/**
 * This is dedicated to real-time games. This is necessary as real-time move forward even in absence of player explicit
 * move.
 * 
 * @author Benoit Lacelle
 *
 */
@Slf4j
public class RealTimeBoardManager extends BoardLifecycleManager implements InitializingBean {

	final Map<UUID, ScheduledFuture<?>> contestIdToLoop = new ConcurrentHashMap<>();

	// This is responsible for executing time-progress: it will rely itself on the boardEvolutionExecutor
	// There is a design flow: the scheduleExecutorThread would remain pending while the boardEvolutionExecutor is
	// working
	final ScheduledExecutorService timeLoopExecutor = Executors.newScheduledThreadPool(16);

	public RealTimeBoardManager(BoardLifecycleManagerHelper helper, Executor boardEvolutionExecutor) {
		super(helper, boardEvolutionExecutor);

		timeLoopExecutor
				.execute(() -> log.info("This flags the timeLoop. threadName={}", Thread.currentThread().getName()));
	}

	@Override
	protected UUID doGameover(UUID contestId, boolean force) {
		UUID boardStateId = super.doGameover(contestId, force);

		ScheduledFuture<?> future = contestIdToLoop.get(contestId);
		if (future != null) {
			log.info("Cancelling the future for contestId={}", contestId);
			future.cancel(true);
		}

		return boardStateId;
	}

	@Subscribe
	public void onContestIsCreated(ContestIsCreated event) {
		Contest contest = helper.getContestsRegistry().getContest(event.getContestId());

		if (contest.getGame() instanceof IHasRealtimeGame) {
			startContestRealtimeLoop(contest.getContestId());
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		helper.getContestsRegistry()
				.searchContests(ContestSearchParameters.builder()
						.gameOver(false)
						.requiredTag(IGameMetadataConstants.TAG_REALTIME)
						.build())
				.forEach(contest -> {
					startContestRealtimeLoop(contest.getContestId());
				});
	}

	protected void cancelTimeLoop(UUID contestId) {
		log.info("We cancel the scheduled realtimeLoop");
		throw new IllegalStateException("We cancel the timeLoop with an exception");
	}

	public void startContestRealtimeLoop(UUID contestId) {
		Contest contest = helper.getContestsRegistry().getContest(contestId);

		if (contest.getGame() instanceof IHasRealtimeGame realtimeGame) {
			ScheduledFuture<?> future = scheduleRealtimeLoop(realtimeGame.getRealtimeGame(), contest);
			ScheduledFuture<?> previousFtuture = contestIdToLoop.putIfAbsent(contestId, future);
			if (previousFtuture != null) {
				future.cancel(true);
				throw new IllegalStateException("Multiple .startContestRealtimeLoop for contestId=" + contestId);
			}
		} else {
			throw new IllegalStateException(
					"gameId=%s is not realtime".formatted(contest.getGameMetadata().getGameId()));
		}

	}

	protected ScheduledFuture<?> scheduleRealtimeLoop(IRealtimeGame realtimeGame, Contest contest) {
		Duration pace = realtimeGame.getPace();
		log.info("Scheduling the timeLoop for contestId={} at pace={}", contest.getContestId(), pace);

		AtomicLong previousFrame = new AtomicLong(System.nanoTime());

		// `.scheduleAtFixedRate` will not enable concurrent execution in case of slow/delayed execution
		ScheduledFuture<?> future = timeLoopExecutor.scheduleAtFixedRate(() -> {
			try {
				runTimeLoop(realtimeGame, contest, pace, previousFrame);
			} catch (Throwable t) {
				UUID contestId = contest.getContestId();
				log.warn("Issue running timeLoop for contestId={}", contestId, t);
				doGameover(contestId, true);
			}

		}, pace.toNanos(), pace.toNanos(), TimeUnit.NANOSECONDS);

		return future;
	}

	private void runTimeLoop(IRealtimeGame realtimeGame, Contest contest, Duration pace, AtomicLong previousFrame) {
		UUID contestId = contest.getContestId();

		if (helper.isGameover(contestId)) {
			// The game is over: stop the scheduled task
			cancelTimeLoop(contestId);

			// The game is over: as it may be due to time, we need to register gameOver as a gameEvent
			executeBoardChange(contestId, board -> {
				// Mark gameOver while inside the loop. It will prevent other interactions
				log.info("timeFlow led to gameOver for contestId={}", contestId);
				return doGameover(contestId, false);
			});

		} else {
			long nanos = System.nanoTime();
			long nanosSincePreviousFrame = nanos - previousFrame.getAndSet(nanos);
			long nanosUntilNextFrame = pace.toNanos() - nanosSincePreviousFrame;

			int nbFrameForward;
			if (nanosUntilNextFrame < 0) {
				long nbFrameAsLong = 1 - nanosUntilNextFrame / pace.toNanos();
				nbFrameForward = (int) Math.min(Integer.MAX_VALUE, nbFrameAsLong);

				if (nbFrameForward >= 2) {
					log.warn("Updating the board took longer than the page of the game ({}ms > {})",
							TimeUnit.NANOSECONDS.toMillis(-nanosUntilNextFrame),
							pace);
				}
			} else {
				try {
					TimeUnit.NANOSECONDS.sleep(nanosUntilNextFrame);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new IllegalStateException(e);
				}
				nbFrameForward = 1;
			}

			executeBoardChange(contestId, board -> {
				IKumiteBoard forwardedBoard = realtimeGame.forward(board, nbFrameForward);

				saveUpdatedBoard(contestId, forwardedBoard);

				// This boardUpdateId is fake, though it should not be used at any point as this
				// `executeBoardChange` is scheduled
				return UUID.randomUUID();
			});
		}
	}

}
