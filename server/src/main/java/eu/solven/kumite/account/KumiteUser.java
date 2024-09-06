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
	// IKumiteSpringProfiles.P_DEFAULT_FAKE_USER
	public static final UUID FAKE_ACCOUNT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

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
