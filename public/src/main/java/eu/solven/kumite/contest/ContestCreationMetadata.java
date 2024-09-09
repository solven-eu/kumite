package eu.solven.kumite.contest;

import java.util.UUID;

import eu.solven.kumite.game.GameMetadata;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * The set of metadata required to create a Contest. These are final values.
 * 
 * @author Benoit Lacelle
 *
 */
@Value
@Builder
@Jacksonized
public class ContestCreationMetadata {
	@NonNull
	UUID gameId;

	@NonNull
	String name;

	// Boxed integer to ensure this is properly initialized
	// `>= game.minPlayers`
	@NonNull
	Integer minPlayers;

	// `>= game.minPlayers`
	@NonNull
	Integer maxPlayers;

	public static ContestCreationMetadataBuilder fromGame(GameMetadata game) {
		return ContestCreationMetadata.builder()
				.gameId(game.getGameId())
				.minPlayers(game.getMinPlayers())
				.maxPlayers(game.getMaxPlayers());
	}

}
