package eu.solven.kumite.player;

import java.util.UUID;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class PlayerJoinRaw {
	UUID playerId;
	UUID contestId;

	// A viewing player does not play the game, but can see everything on the board. It is typically interesting for
	// humans to look at running games
	@Default
	boolean isViewer = false;
}
