package eu.solven.kumite.player;

import java.util.List;

/**
 * Generally provided by {@link ContestPlayersRegistry}
 * 
 * @author Benoit Lacelle
 *
 */
public interface IHasPlayers {
	List<KumitePlayer> getPlayers();
}
