package eu.solven.kumite.player;

import java.util.UUID;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class KumitePlayer {
	@NonNull
	UUID playerId;

	// UUID accountId;

	// Nice-name, editable by the account
	// String name;
}
