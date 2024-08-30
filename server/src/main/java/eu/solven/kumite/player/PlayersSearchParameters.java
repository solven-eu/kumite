package eu.solven.kumite.player;

import java.util.Optional;
import java.util.UUID;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Value;

@Value
@Builder
public class PlayersSearchParameters {
	@Default
	Optional<UUID> contestId = Optional.empty();

	@Default
	Optional<UUID> accountId = Optional.empty();

}
