package eu.solven.kumite.contest;

import java.util.UUID;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class ContestMetadataRaw {
	@NonNull
	@Default
	UUID contestId = UUID.randomUUID();

	@NonNull
	UUID gameId;

	boolean beingPlayed;
	boolean acceptPlayers;

	public static ContestMetadataRaw snapshot(IContest contest) {
		return ContestMetadataRaw.builder()
				.contestId(contest.getContestId())
				.gameId(contest.getGameMetadata().getGameId())
				.acceptPlayers(contest.isAcceptPlayers())
				.beingPlayed(contest.isGameOver())
				.build();
	}

}
