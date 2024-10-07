package eu.solven.kumite.contest;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;

import eu.solven.kumite.game.GameMetadata;
import eu.solven.kumite.game.IGameMetadataConstants;
import lombok.Builder;
import lombok.Builder.Default;
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

	// accountId
	@NonNull
	UUID author;

	@Default
	OffsetDateTime created = OffsetDateTime.now(Clock.systemUTC());

	OffsetDateTime timeout;

	public static ContestCreationMetadataBuilder fromGame(GameMetadata game) {
		return ContestCreationMetadata.builder()
				.gameId(game.getGameId())
				.minPlayers(game.getMinPlayers())
				.maxPlayers(game.getMaxPlayers());
	}

	/**
	 * 
	 * @return an invalid {@link ContestCreationMetadata}. Useful in edge-cases, like processing race-conditions and
	 *         removed contests.
	 */
	public static ContestCreationMetadata empty() {
		return ContestCreationMetadata.builder()
				.gameId(IGameMetadataConstants.EMPTY)
				.name("empty")
				.author(UUID.fromString("12345678-1234-1234-1234-123456789012"))
				.minPlayers(Integer.MIN_VALUE)
				.maxPlayers(Integer.MIN_VALUE)
				.build();
	}

}
