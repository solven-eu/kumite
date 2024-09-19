package eu.solven.kumite.player;

import java.util.UUID;

import eu.solven.kumite.move.PlayerMoveRaw;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class PlayerMove {
	@NonNull
	PlayerMoveRaw playerMove;
	// UUID playerId;
	@NonNull
	UUID contestId;
	// @NonNull
	// IKumiteMove move;
}
