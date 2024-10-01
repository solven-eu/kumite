package eu.solven.kumite.account.internal;

import java.util.UUID;

import eu.solven.kumite.account.KumiteUserDetails;
import eu.solven.kumite.player.KumitePlayer;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;

/**
 * User details, not including the OAuth2 provider and its sub.
 * 
 * @author Benoit Lacelle
 *
 */
@Value
@Builder
@Jacksonized
// This is not public, but used for persistence, IKumiteTestConstants and `player` module
@Slf4j
public class KumiteUserRaw {
	// Multiple rawRaw may be attached to the same account (e.g. by using different OAuth2 providers)
	@NonNull
	UUID accountId;

	@NonNull
	KumiteUserDetails details;

	@Default
	boolean enabled = true;

	// Each account has a default playerId.
	@NonNull
	UUID playerId;

	public KumitePlayer mainPlayer() {
		return KumitePlayer.builder().accountId(accountId).playerId(playerId).build();
	}

	public KumiteUserRaw editRaw(KumiteUserDetails details) {
		return KumiteUserRaw.builder()
				.accountId(accountId)
				.details(details)
				.enabled(enabled)
				.playerId(playerId)
				.build();
	}

}
