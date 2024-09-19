package eu.solven.kumite.player;

import java.util.Optional;
import java.util.UUID;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Value;

@Value
@Builder
public class PlayerSearchParameters {
	// What is the useCase to search for a specific player, without knowing its accountId or contestId?
	// The UI enables looking for a given playerId
	@Default
	Optional<UUID> playerId = Optional.empty();

	@Default
	Optional<UUID> contestId = Optional.empty();

	@Default
	Optional<UUID> accountId = Optional.empty();

}
