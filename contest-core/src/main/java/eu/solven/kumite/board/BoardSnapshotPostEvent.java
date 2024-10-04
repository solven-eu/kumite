package eu.solven.kumite.board;

import java.util.Set;
import java.util.UUID;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BoardSnapshotPostEvent {
	IKumiteBoard board;
	
	Set<UUID> enabledPlayerIds;
}
