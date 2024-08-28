package eu.solven.kumite.account;

import java.util.UUID;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

/**
 * An account refers to the identity of a human. A human may manage multiple players.
 * 
 * @author Benoit Lacelle
 *
 */
@Value
@Builder
public class KumiteAccount {
	@NonNull
	UUID accountId;

	// Each account has a default playerId.
	@NonNull
	UUID playerId;

	// @Singular
	// Set<UUID> playerIds = Set.of();
}
