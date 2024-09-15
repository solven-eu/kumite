package eu.solven.kumite.board;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.solven.kumite.player.KumitePlayer;
import eu.solven.kumite.player.PlayerMoveRaw;

/**
 * Needs to be serializable to {@link String} by {@link ObjectMapper}.
 * 
 * @author Benoit Lacelle
 *
 */
public interface IKumiteBoard {
	// Some games may need to register some player parameters
	void registerPlayer(UUID playerId);

	List<String> isValidMove(PlayerMoveRaw playerMove);

	void registerMove(PlayerMoveRaw playerMove);

	IKumiteBoardView asView(UUID playerId);

	List<KumitePlayer> snapshotPlayers();
}
