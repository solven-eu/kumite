package eu.solven.kumite.game;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class GameMetadata implements IGameMetadataConstants {

	@NonNull
	UUID gameId;

	@NonNull
	String title;

	@NonNull
	String shortDescription;

	@Singular
	List<URI> references;

	// Tags mus not contains `,`, which is used as OR separator
	@Singular
	List<String> tags;

	// These are not definitive values, as some game may be lax in term of number of players, while a contest may have
	// stricter constrains.
	// Typically, an Optimization contest may require exactly 4 players, while the game itself can accept any number of
	// players.
	@Default
	int minPlayers = 1;
	@Default
	int maxPlayers = 1;
}
