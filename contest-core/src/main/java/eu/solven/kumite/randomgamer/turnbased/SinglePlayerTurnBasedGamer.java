package eu.solven.kumite.randomgamer.turnbased;

import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;

import eu.solven.kumite.board.BoardLifecycleManager;
import eu.solven.kumite.game.IGameMetadataConstants;
import eu.solven.kumite.randomgamer.GamerLogicHelper;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * This holds a contender logic, playing randomly amongst available moves. It is useful for integration-tests.
 * 
 * @author Benoit Lacelle
 *
 */
@Slf4j
public class SinglePlayerTurnBasedGamer extends ATurnBasedGamerLogic {

	@NonNull
	final UUID playerId;

	public SinglePlayerTurnBasedGamer(GamerLogicHelper gamerLogicHelper,
			@Qualifier(IGameMetadataConstants.TAG_TURNBASED) BoardLifecycleManager boardLifecycleManager,
			UUID playerId) {
		super(gamerLogicHelper, boardLifecycleManager);

		this.playerId = playerId;
	}

	@Override
	protected Set<UUID> playerCandidates() {
		return Set.of(playerId);
	}

	@Override
	protected boolean isPlayerCandidate(UUID playerId) {
		return playerId.equals(playerId);
	}

}
