package eu.solven.kumite.account.internal;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

import eu.solven.kumite.account.KumiteUserDetails;
import eu.solven.kumite.account.KumiteUserRawRaw;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

/**
 * Lacks the accountId.
 * 
 * @author Benoit Lacelle
 *
 */
@Value
@Builder
@Slf4j
// This is not public, but used for IKumiteTestConstants and `player` module
@JsonIgnoreType
public class KumiteUserPreRegister {

	@NonNull
	KumiteUserRawRaw rawRaw;

	@NonNull
	KumiteUserDetails details;

}
