package eu.solven.kumite.board;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * The set of board metadata which could evolve, and are not stored into the {@link IKumiteBoard}, independently of
 * current player.
 * 
 * @author Benoit Lacelle
 *
 */
@Value
@Builder
@Jacksonized
public class BoardDynamicMetadata {

	@Default
	Map<UUID, OffsetDateTime> playerIdToLastMove = new TreeMap<>();

	OffsetDateTime gameOverTs;

	public BoardDynamicMetadata setGameOver() {
		if (gameOverTs != null) {
			// We are already gameOver: do not update its value
			return this;
		}

		return BoardDynamicMetadata.builder()
				.playerIdToLastMove(playerIdToLastMove)
				.gameOverTs(OffsetDateTime.now().atZoneSameInstant(ZoneOffset.UTC).toOffsetDateTime())
				.build();
	}

}
