package eu.solven.kumite.contest;

import java.util.List;
import java.util.UUID;

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
	GameMetadata gameMetadata;

	@Getter(value = AccessLevel.NONE)
	IHasPlayers hasPlayers;

	@Default
	boolean gameOver = false;

	@Override
	public List<KumitePlayer> getPlayers() {
		return hasPlayers.getPlayers();
	}

}
