package eu.solven.kumite.game;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.UUID;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Singular;
import lombok.Value;

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
}
