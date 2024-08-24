package eu.solven.kumite.player;

import java.util.UUID;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class PlayerMove {
	UUID playerId;
	UUID contestId;
	IKumiteMove move;
}
