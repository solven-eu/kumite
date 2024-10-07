package eu.solven.kumite.randomgamer.turnbased;

import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;

import eu.solven.kumite.account.fake_player.FakePlayer;
import eu.solven.kumite.board.BoardLifecycleManager;
import eu.solven.kumite.game.IGameMetadataConstants;
import eu.solven.kumite.randomgamer.GamerLogicHelper;
import lombok.extern.slf4j.Slf4j;

/**
 * This holds a contender logic, playing randomly amongst available moves. It is useful for integration-tests.
 * 
 * @author Benoit Lacelle
 *
 */
@Slf4j
public class FakeTurnBasedGamer extends ATurnBasedGamerLogic {

	public FakeTurnBasedGamer(GamerLogicHelper gamerLogicHelper,
			@Qualifier(IGameMetadataConstants.TAG_TURNBASED) BoardLifecycleManager boardLifecycleManager) {
		super(gamerLogicHelper, boardLifecycleManager);
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
