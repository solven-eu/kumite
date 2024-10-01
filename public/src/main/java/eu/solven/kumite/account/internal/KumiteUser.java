package eu.solven.kumite.account.internal;

import java.util.UUID;

import eu.solven.kumite.account.KumiteUserDetails;
import eu.solven.kumite.account.KumiteUserRawRaw;
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
// This is not public, but used for persistence, IKumiteTestConstants and `player` module
public class KumiteUser {
	// Multiple rawRaw may be attached to the same account (e.g. by using different OAuth2 providers)
	@NonNull
	UUID accountId;

	@NonNull
	KumiteUserRawRaw rawRaw;

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

	public KumiteUser editDetails(KumiteUserDetails details) {
		return KumiteUser.builder()
				.accountId(accountId)
				.rawRaw(rawRaw)
				.details(details)
				.enabled(enabled)
				.playerId(playerId)
				.build();
	}

	public static KumiteUserRaw raw(KumiteUser user) {
		return KumiteUserRaw.builder()
				.accountId(user.accountId)
				.details(user.details)
				.enabled(user.enabled)
				.playerId(user.playerId)
				.build();
	}

	public static KumiteUserPreRegister pre(KumiteUser user) {
		return KumiteUserPreRegister.builder().rawRaw(user.getRawRaw()).details(user.getDetails()).build();
	}

}
