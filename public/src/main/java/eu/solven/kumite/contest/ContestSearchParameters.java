package eu.solven.kumite.contest;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Singular;
import lombok.Value;

/**
 * The parameters to search amongst contests.
 * 
 * @author Benoit Lacelle
 *
 */
@Value
@Builder
public class ContestSearchParameters {
	@Default
	Optional<UUID> contestId = Optional.empty();

	@Default
	Optional<UUID> gameId = Optional.empty();

	// @Default
	// OptionalInt minPlayers = OptionalInt.empty();
	// @Default
	// OptionalInt maxPlayers = OptionalInt.empty();

	// Some game may accept players while being started
	// `null` means no filter
	@Default
	Boolean acceptPlayers = null;

	// Typically if the minimum number of players is not yet reached
	// `null` means no filter
	@Default
	Boolean requirePlayers = null;

	// `null` means no filter
	@Default
	Boolean gameOver = null;

	// This is an AND conditions on the tags
	// Each String may be a list of coma-separated tags, which would express and OR
	@Singular
	Set<String> requiredTags;

	public static ContestSearchParameters searchContestId(UUID contestId) {
		return ContestSearchParameters.builder()
				// Search for given contestId
				.contestId(Optional.of(contestId))
				// Cancel the sensible defaults
				.gameOver(null)
				.build();
	}
}
