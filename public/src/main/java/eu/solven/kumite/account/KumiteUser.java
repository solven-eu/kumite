package eu.solven.kumite.account;

import java.util.UUID;

import eu.solven.kumite.player.KumitePlayer;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;

/**
 * User details, typically from an oauth2 provider
 * 
 * @author Benoit Lacelle
 *
 */
@Value
@Builder
@Jacksonized
@Slf4j
public class KumiteUser {
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

	public KumitePlayer mainPlayer() {
		return KumitePlayer.builder().accountId(accountId).playerId(playerId).build();
	}

}
