package eu.solven.kumite.contest;

import java.util.UUID;
import java.util.stream.Collectors;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * A serializable view of {@link ContestMetadata}.
 * 
 * @author Benoit Lacelle
 *
 */
@Value
@Builder
@Jacksonized
public class ContestMetadataRaw {
	@NonNull
	UUID contestId;

	@NonNull
	ContestCreationMetadata constantMetadata;

	@NonNull
	ContestDynamicMetadata dynamicMetadata;

	public static ContestMetadataRaw snapshot(ContestMetadata contest) {
		return ContestMetadataRaw.builder()
				.contestId(contest.getContestId())
				.constantMetadata(contest.getConstantMetadata())
				.dynamicMetadata(ContestDynamicMetadata.builder()
						.acceptingPlayers(contest.isAcceptingPlayers())
						.requiringPlayers(contest.isRequiringPlayers())
						.gameOver(contest.isGameOver())
						.players(contest.getPlayers().stream().map(p -> p.getPlayerId()).collect(Collectors.toSet()))
						.build())
				.build();
	}

}
