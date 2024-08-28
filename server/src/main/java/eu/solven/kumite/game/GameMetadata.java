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
public class GameMetadata {
	@NonNull
	UUID gameId;

	@NonNull
	String title;

	@NonNull
	String shortDescription;

	@Singular
	List<URI> references;

	@Default
	int minPlayers = 1;
	@Default
	int maxPlayers = 1;
}
