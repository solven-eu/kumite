package eu.solven.kumite.randomgamer.realtime;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import org.greenrobot.eventbus.EventBus;
import org.springframework.beans.factory.annotation.Qualifier;

import eu.solven.kumite.account.fake_player.RandomPlayer;
import eu.solven.kumite.board.realtime.RealTimeBoardManager;
import eu.solven.kumite.game.IGameMetadataConstants;
import eu.solven.kumite.move.IKumiteMove;
import eu.solven.kumite.randomgamer.GamerLogicHelper;
import eu.solven.kumite.randomgamer.turnbased.ATurnBasedGamerLogic;
import lombok.extern.slf4j.Slf4j;

/**
 * This holds a contender logic, playing randomly amongst available moves. It is useful for integration-tests.
 * 
 * @author Benoit Lacelle
 *
 */
@Slf4j
public class RandomRealTimeGamer extends ATurnBasedGamerLogic {

	final EventBus eventBus;

	public RandomRealTimeGamer(GamerLogicHelper gamerLogicHelper,
			@Qualifier(IGameMetadataConstants.TAG_REALTIME) RealTimeBoardManager boardLifecycleManager,
			EventBus eventBus) {
		super(gamerLogicHelper, boardLifecycleManager);

		this.eventBus = eventBus;
	}

	@Override
	protected Map.Entry<String, IKumiteMove> pickMove(List<Entry<String, IKumiteMove>> playableMoves) {
		return playableMoves.get(getGamerLogicHelper().getRandomGenerator().nextInt(playableMoves.size()));
	}

	@Override
	protected Set<UUID> playerCandidates() {
		return RandomPlayer.playerIds();
	}

	@Override
	protected boolean isPlayerCandidate(UUID playerId) {
		return RandomPlayer.isRandomPlayer(playerId);
	}

	public void waitBoardUpdate(UUID contestId, UUID previousKnownBoardStateId) throws InterruptedException {
		// Zero: we check for the board if it has already changed
		{
			UUID currentBoardStateId =
					getGamerLogicHelper().getBoardsRegistry().getMetadata(contestId).get().getBoardStateId();
			if (!currentBoardStateId.equals(previousKnownBoardStateId)) {
				// The board has evolved: do not need to wait for any event
				return;
			}
		}

		// This is especially useful for real-time as if the gamer thinks faster than the gameLoop, the player often has
		// to wait. We note such a method could also be useful for TuneBased games.
		CountDownLatch cdl = new CountDownLatch(1);

		Object eventSubscriber = eventSubscriber(cdl);

		try {
			// First, we register ourselves on the EventBus
			eventBus.register(eventSubscriber);

			// Second, we check for current boardStateId
			UUID currentBoardStateId =
					getGamerLogicHelper().getBoardsRegistry().getMetadata(contestId).get().getBoardStateId();
			if (!currentBoardStateId.equals(previousKnownBoardStateId)) {
				// The board has evolved: do not need to wait for any event
				log.info("boardUpdated between event.subscribe() and cdl.await()");
				return;
			}

			// Third, we wait for anything updating the board
			log.info("await for boardUpdated");
			cdl.await();
		} finally {
			// Unregister this listener as it is not used anymore
			eventBus.unregister(eventSubscriber);
		}
	}

	private Object eventSubscriber(CountDownLatch cdl) {
		return new BoardIsUpdatedEventSubscriber(event -> {
			cdl.countDown();
		});
	}

}
