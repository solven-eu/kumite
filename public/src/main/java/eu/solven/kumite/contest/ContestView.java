package eu.solven.kumite.contest;

import java.util.Map;
import java.util.UUID;

import eu.solven.kumite.player.PlayerContestStatus;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * A snapshot of the Contest
 * 
 * @author Benoit Lacelle
 *
 */
@Value
@Builder
@Jacksonized
public class ContestView {
	@NonNull
	UUID contestId;

	@NonNull
	PlayerContestStatus playerStatus;

	@NonNull
	ContestDynamicMetadata dynamicMetadata;

	// Could be turned into a IKumiteBoardView by an IGame
	@NonNull
	Map<String, ?> board;
}
