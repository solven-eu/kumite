package eu.solven.kumite.game;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Singular;
import lombok.Value;

/**
 * Options to search through games.
 * 
 * @author Benoit Lacelle
 */
@Value
@Builder
public class GameSearchParameters {
	@Default
	Optional<UUID> gameId = Optional.empty();

	@Default
	OptionalInt minPlayers = OptionalInt.empty();
	@Default
	OptionalInt maxPlayers = OptionalInt.empty();

	// This is an AND conditions on the tags
	// Each String may be a list of coma-separated tags, which would express and OR
	@Singular
	Set<String> requiredTags;

	@Default
	Optional<String> titleRegex = Optional.empty();

	// Tags can be OR-ed by being joined with a ','.
	public static String or(String firstTag, String... moreTags) {
		return Stream.concat(Stream.of(firstTag), Stream.of(moreTags)).peek(tag -> {
			if (tag.contains(",")) {
				throw new IllegalArgumentException("An individual tag must not contain a ','");
			}
		}).collect(Collectors.joining(","));
	}

	public static GameSearchParameters byGameId(UUID gameId) {
		return GameSearchParameters.builder().gameId(Optional.of(gameId)).build();
	}
}
