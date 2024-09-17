package eu.solven.kumite.account;

import java.util.UUID;

import lombok.Builder;
import lombok.Builder.Default;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * User details, typically from an oauth2 provider
 * 
 * @author Benoit Lacelle
 *
 */
@Value
@Builder
@Jacksonized
public class KumiteUser {
	// Used to create contests
	public static final UUID SERVER_ACCOUNTID = UUID.fromString("FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF");

	// Multiple Users may be attached to the same account (e.g. by using different OAuth2 providers)
	@NonNull
	UUID accountId;

	@NonNull
	KumiteUserRaw raw;

	@Default
	boolean enabled = true;

	// Each account has a default playerId.
	@NonNull
	UUID playerId;
}
