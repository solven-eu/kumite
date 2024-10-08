package eu.solven.kumite.randomgamer.turnbased;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import eu.solven.kumite.account.fake_player.RandomPlayer;
import eu.solven.kumite.board.BoardLifecycleManager;
import eu.solven.kumite.move.IKumiteMove;
import eu.solven.kumite.randomgamer.GamerLogicHelper;
import lombok.extern.slf4j.Slf4j;

/**
 * This holds a contender logic, playing randomly amongst available moves. It is useful for integration-tests.
 * 
 * @author Benoit Lacelle
 *
 */
@Slf4j
public class RandomTurnBasedGamer extends ATurnBasedGamerLogic {

	public RandomTurnBasedGamer(GamerLogicHelper gamerLogicHelper, BoardLifecycleManager boardLifecycleManager) {
		super(gamerLogicHelper, boardLifecycleManager);
	}

	@Override
	protected Entry<String, IKumiteMove> pickMove(List<Entry<String, IKumiteMove>> playableMoves) {
		return playableMoves.get(gamerLogicHelper.getRandomGenerator().nextInt(playableMoves.size()));
	}

	@Override
	protected Set<UUID> playerCandidates() {
		return RandomPlayer.playerIds();
	}

	@Override
	protected boolean isPlayerCandidate(UUID playerId) {
		return RandomPlayer.isRandomPlayer(playerId);
	}

}
