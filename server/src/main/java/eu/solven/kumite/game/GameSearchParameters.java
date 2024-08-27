package eu.solven.kumite.game;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

import lombok.Builder;
import lombok.Builder.Default;
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

	@Default
	Optional<String> titlePattern = Optional.empty();
}
