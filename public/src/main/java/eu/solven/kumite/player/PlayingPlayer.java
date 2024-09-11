package eu.solven.kumite.player;

import java.util.UUID;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class PlayingPlayer {
	@NonNull
	UUID playerId;

	// Has joined as a player, not a viewer
	boolean playerHasJoined;
	boolean playerCanJoin;
	boolean accountIsViewing;

	public static PlayingPlayer player(UUID playerId) {
		return PlayingPlayer.builder().playerId(playerId).playerHasJoined(true).build();
	}

	public static PlayingPlayer viewer(UUID playerId) {
		return PlayingPlayer.builder().playerId(playerId).accountIsViewing(true).build();
	}
}
