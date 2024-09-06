package eu.solven.kumite.contest;

import java.util.UUID;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class ContestMetadataRaw {
	@NonNull
	UUID contestId;

	@NonNull
	UUID gameId;

	@NonNull
	String name;

	@NonNull
	// Boxed integer to ensure this is properly initialized
	Integer nbActivePlayers;

	boolean beingPlayed;
	boolean acceptPlayers;

	public static ContestMetadataRaw snapshot(ContestMetadata contest) {
		return ContestMetadataRaw.builder()
				.contestId(contest.getContestId())
				.name(contest.getName())
				.gameId(contest.getGameMetadata().getGameId())
				.acceptPlayers(contest.isAcceptPlayers())
				.beingPlayed(contest.isGameOver())
				.nbActivePlayers(contest.getPlayers().size())
				.build();
	}

}
