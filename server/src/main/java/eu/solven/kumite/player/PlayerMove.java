package eu.solven.kumite.player;

import java.util.UUID;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class PlayerMove {
	@NonNull
	UUID playerId;
	@NonNull
	UUID contestId;
	@NonNull
	IKumiteMove move;
}
