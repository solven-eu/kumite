package eu.solven.kumite.account;

import java.net.URI;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * Like {@link KumiteUser} but without knowledge of the accountId.
 * 
 * @author Benoit Lacelle
 *
 */
@Value
@Builder
@Jacksonized
public class KumiteUserRaw {
	@NonNull
	KumiteUserRawRaw rawRaw;

	@NonNull
	String username;

	String name;

	String email;

	URI picture;
}
