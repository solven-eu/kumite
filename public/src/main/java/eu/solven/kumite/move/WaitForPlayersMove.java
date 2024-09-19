package eu.solven.kumite.move;

import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * An informative set of waited players. This may be informative or empty for games with ImperfectImformation.
 * 
 * It holds the playerId of waitedPlayers.
 */
@Value
@Builder
@Jacksonized
@JsonIgnoreProperties(value = "wait", allowGetters = true)
public class WaitForPlayersMove implements IKumiteMove {

	// @JsonProperty(access = JsonProperty.Access.READ_ONLY)
	final boolean wait = true;

	// Javadoc leads to an Eclipse bug, seemingly related with https://github.com/projectlombok/lombok/issues/3706
	@Singular
	Set<UUID> waitedPlayers;
}
