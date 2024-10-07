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
	// This is helps knowing (from player-side) if a board has been played or not
	@Default
	UUID boardStateId = UUID.randomUUID();

	@Default
	Map<UUID, OffsetDateTime> playerIdToLastMove = new TreeMap<>();

	OffsetDateTime gameOverTs;

	private BoardDynamicMetadataBuilder prepareMutation() {
		return BoardDynamicMetadata.builder().playerIdToLastMove(playerIdToLastMove).gameOverTs(gameOverTs);
	}

	public BoardDynamicMetadata touch() {
		if (gameOverTs != null) {
			// We are already gameOver: do not update its value
			return this;
		}

		return prepareMutation().build();
	}

	public BoardDynamicMetadata setGameOver() {
		if (gameOverTs != null) {
			// We are already gameOver: do not update its value
			return this;
		}

		return prepareMutation().gameOverTs(now()).build();
	}

	public BoardDynamicMetadata playerMoved(UUID playerId) {
		Map<UUID, OffsetDateTime> updatedPlayerIdToLastMove = new TreeMap<>(playerIdToLastMove);

		updatedPlayerIdToLastMove.put(playerId, now());

		return prepareMutation().playerIdToLastMove(updatedPlayerIdToLastMove).build();
	}

	public static OffsetDateTime now() {
		return OffsetDateTime.now().atZoneSameInstant(ZoneOffset.UTC).toOffsetDateTime();
	}
}
