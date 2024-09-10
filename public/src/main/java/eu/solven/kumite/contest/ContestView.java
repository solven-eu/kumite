package eu.solven.kumite.contest;

import java.util.UUID;

import eu.solven.kumite.board.IKumiteBoardView;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class ContestView {
	UUID contestId;
	UUID playerId;

	ContestDynamicMetadata dynamicMetadata;

	IKumiteBoardView board;

	@NonNull
	Boolean playerHasJoined;

	@NonNull
	Boolean playerCanJoin;

	@NonNull
	Boolean accountIsViewing;
}
