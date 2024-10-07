package eu.solven.kumite.board;

import java.util.Set;
import java.util.UUID;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class BoardSnapshotPostEvent {
	@NonNull
	UUID boardStateId;

	@NonNull
	IKumiteBoard board;

	@NonNull
	Set<UUID> enabledPlayerIds;
}
