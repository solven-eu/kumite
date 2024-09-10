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
	Optional<UUID> contestId = Optional.empty();

	@Default
	Optional<UUID> gameId = Optional.empty();

	// @Default
	// OptionalInt minPlayers = OptionalInt.empty();
	// @Default
	// OptionalInt maxPlayers = OptionalInt.empty();

	// Some game may accept players while being started
	@Default
	boolean acceptPlayers = true;

	// Typically if the minimum number of players is not yet reached
	@Default
	boolean requirePlayers = false;

	@Default
	boolean gameOver = false;
}
