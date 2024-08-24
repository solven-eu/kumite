package eu.solven.kumite.contest;

import java.util.Optional;
import java.util.UUID;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Value;

@Value
@Builder
public class ContestSearchParameters {
	@Default
	Optional<UUID> contestUuid = Optional.empty();

	@Default
	Optional<UUID> gameUuid = Optional.empty();

	// @Default
	// OptionalInt minPlayers = OptionalInt.empty();
	// @Default
	// OptionalInt maxPlayers = OptionalInt.empty();

	@Default
	boolean acceptPlayers = true;

	@Default
	boolean beingPlayed = true;
}
