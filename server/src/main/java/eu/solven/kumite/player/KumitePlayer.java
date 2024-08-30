package eu.solven.kumite.player;

import java.util.UUID;

import eu.solven.kumite.account.KumiteAccount;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * Represent a player, i.e. some entity able to play contests. A single {@link KumiteAccount} (representing a
 * User/Human) could manage multiple {@link KumitePlayer} (e.g. for tracking different algorithms).
 * 
 * @author Benoit Lacelle
 *
 */
@Value
@Builder
@Jacksonized
public class KumitePlayer {
	@NonNull
	UUID playerId;

	// UUID accountId;

	// Nice-name, editable by the account
	// String name;
}
