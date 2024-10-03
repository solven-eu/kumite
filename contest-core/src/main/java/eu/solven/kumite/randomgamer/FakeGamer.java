package eu.solven.kumite.randomgamer;

import java.util.Set;
import java.util.UUID;

import eu.solven.kumite.account.fake_player.FakePlayer;
import lombok.extern.slf4j.Slf4j;

/**
 * This holds a contender logic, playing randomly amongst available moves. It is useful for integration-tests.
 * 
 * @author Benoit Lacelle
 *
 */
@Slf4j
public class FakeGamer extends AGamerLogic {

	public FakeGamer(GamerLogicHelper gamerLogicHelper) {
		super(gamerLogicHelper);
	}

	@Override
	protected Set<UUID> playerCandidates() {
		return FakePlayer.playersIds();
	}

	@Override
	protected boolean isPlayerCandidate(UUID playerId) {
		return FakePlayer.isFakePlayer(playerId);
	}

}
