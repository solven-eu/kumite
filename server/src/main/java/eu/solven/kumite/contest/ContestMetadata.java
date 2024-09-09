package eu.solven.kumite.contest;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import eu.solven.kumite.game.GameMetadata;
import eu.solven.kumite.player.IHasPlayers;
import eu.solven.kumite.player.KumitePlayer;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
public class ContestMetadata implements IContest {
	@NonNull
	UUID contestId;

	@NonNull
	ContestCreationMetadata constantMetadata;

	@NonNull
	GameMetadata gameMetadata;

	@Getter(value = AccessLevel.NONE)
	IHasPlayers hasPlayers;

	@Default
	boolean gameOver = false;

	@Override
	public List<KumitePlayer> getPlayers() {
		return hasPlayers.getPlayers();
	}


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
