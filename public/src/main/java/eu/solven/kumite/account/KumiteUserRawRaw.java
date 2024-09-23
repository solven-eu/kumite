package eu.solven.kumite.account;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * Like {@link KumiteUser} but without knowledge of the accountId.
 * 
 * Serializable as may be persisted in Redis.
 * 
 * @author Benoit Lacelle
 *
 */
@Value
@Builder
@Jacksonized
public class KumiteUserRawRaw {
	// `github` or `google`
	@NonNull
	String providerId;

	@NonNull
	String sub;
}
