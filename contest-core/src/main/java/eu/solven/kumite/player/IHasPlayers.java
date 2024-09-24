package eu.solven.kumite.player;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Generally provided by {@link ContestPlayersRegistry} (or {@link AccountPlayersRegistry}).
 * 
 * @author Benoit Lacelle
 *
 */
public interface IHasPlayers {
	List<KumitePlayer> getPlayers();

	/**
	 * 
	 * @param playerId
	 * @return true if the playerId is amongst the players.
	 */
	default boolean hasPlayerId(UUID playerId) {
		return getPlayers().stream().anyMatch(p -> p.getPlayerId().equals(playerId));
	}

	default Set<UUID> getPlayerIds() {
		return getPlayers().stream().map(p -> p.getPlayerId()).collect(Collectors.toSet());
	}
}
