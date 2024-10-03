package eu.solven.kumite.randomgamer;

import java.util.Set;
import java.util.UUID;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

/**
 * This holds a contender logic, playing randomly amongst available moves. It is useful for integration-tests.
 * 
 * @author Benoit Lacelle
 *
 */
@Slf4j
public class SinglePlayerIdGamer extends AGamerLogic {

	@NonNull
	final UUID playerId;

	public SinglePlayerIdGamer(GamerLogicHelper gamerLogicHelper, UUID playerId) {
		super(gamerLogicHelper);

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
