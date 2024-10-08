package eu.solven.kumite.player;

import java.util.UUID;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * Represent a player, i.e. some entity able to play contests. A single {@link KumiteUser} (representing a User/Human)
 * could manage multiple {@link KumitePlayer} (e.g. for tracking different algorithms).
 * 
 * @author Benoit Lacelle
 *
 */
@Value
@Builder
@Jacksonized
public class KumitePlayer {
	// This is the ID of the preview player, i.e. some fake player viewing the game but not playing.
	// It is useful to preview a given game, without sharing information which would biased the game. It is especially
	// useful for games with ImperfectInformation
	public static final UUID PREVIEW_PLAYER_ID = UUID.fromString("00000000-0000-0000-1111-000000000000");

	// This is the ID of the audience player, i.e. some fake player viewing the game without fog-of-war but not playing.
	// It is useful to watch a given game seeing everything about the board. It is especially
	// useful for games with ImperfectInformation
	public static final UUID AUDIENCE_PLAYER_ID = UUID.fromString("00000000-0000-0000-2222-000000000000");

	@NonNull
	UUID playerId;

	@NonNull
	UUID accountId;

	// public static KumitePlayer fromPlayerId(IAccountPlayersRegistry accountPlayersRegistry, UUID playerId) {
	// UUID accountId = accountPlayersRegistry.getAccountId(playerId);
	// return KumitePlayer.builder().playerId(playerId).accountId(accountId).build();
	// }

	// Nice-name, editable by the account
	// String name;
}
