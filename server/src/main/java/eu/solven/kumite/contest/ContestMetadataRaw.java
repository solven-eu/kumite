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

//	@NonNull
	// Boxed integer to ensure this is properly initialized
//	Integer nbActivePlayers;

	boolean gameOver;
	boolean acceptingPlayers;
	boolean requiringPlayers;

	public static ContestMetadataRaw snapshot(ContestMetadata contest) {
		return ContestMetadataRaw.builder()
				.contestId(contest.getContestId())
				.name(contest.getName())
				.gameId(contest.getGameMetadata().getGameId())
				.acceptingPlayers(contest.isAcceptingPlayers())
				.requiringPlayers(contest.isRequiringPlayers())
				.gameOver(contest.isGameOver())
				// .nbActivePlayers(contest.getPlayers().size())
				.build();
	}

}
