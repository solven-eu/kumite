package eu.solven.kumite.randomgamer;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import eu.solven.kumite.account.fake_player.RandomPlayer;
import eu.solven.kumite.move.IKumiteMove;
import lombok.extern.slf4j.Slf4j;

/**
 * This holds a contender logic, playing randomly amongst available moves. It is useful for integration-tests.
 * 
 * @author Benoit Lacelle
 *
 */
@Slf4j
public class RandomGamer extends AGamerLogic {

	public RandomGamer(GamerLogicHelper gamerLogicHelper) {
		super(gamerLogicHelper);
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
