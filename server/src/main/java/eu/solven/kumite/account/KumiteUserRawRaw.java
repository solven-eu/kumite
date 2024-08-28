package eu.solven.kumite.account;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;

/**
 * Like {@link KumiteUser} but without knowledge of the accountId.
 * 
 * @author Benoit Lacelle
 *
 */
@Value
@Builder
@EqualsAndHashCode
public class KumiteUserRawRaw {
	// `github` or `google`
	@NonNull
	String providerId;

	@NonNull
	String sub;
}
