package eu.solven.kumite.account;

import java.util.Optional;
import java.util.UUID;

import eu.solven.kumite.account.internal.KumiteUser;
import eu.solven.kumite.account.internal.KumiteUserPreRegister;
import eu.solven.kumite.player.KumitePlayer;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class KumiteUsersRegistry {

	// This is a cache of the external information about a user
	// This is useful to enrich some data about other players (e.g. a Leaderboard)
	final IKumiteUserRepository userRepository;

	// We may have multiple users for a single account
	// This maps to the latest/main one
	final IKumiteUserRawRawRepository userRawRawRepository;

	public Optional<KumiteUser> optUser(UUID accountId) {
		return userRawRawRepository.getUser(accountId).map(this::getUser);
	}

	public KumiteUser getUser(UUID accountId) {
		return optUser(accountId).orElseThrow(() -> new IllegalArgumentException("No accountId=" + accountId));
	}

	public KumiteUser getUser(KumiteUserRawRaw rawUser) {
		return userRepository.getUser(rawUser).orElseThrow(() -> new IllegalArgumentException("No rawUser=" + rawUser));
	}

	/**
	 * 
	 * @param kumiteUserRaw
	 * @return a {@link KumiteUser}. This may be a new account if this was not known. If this was already known, we
	 *         update the oauth2 details and return an existing accountId
	 */
	public KumiteUser registerOrUpdate(KumiteUserPreRegister userPreRegister) {
		KumiteUser kumiteUser = userRepository.registerOrUpdate( userPreRegister);

		return kumiteUser;
	}

	public KumitePlayer getAccountMainPlayer(UUID accountId) {
		KumiteUser user = getUser(accountId);
		return KumitePlayer.builder().playerId(user.getPlayerId()).accountId(accountId).build();
	}

}
