package eu.solven.kumite.account.login;

import java.util.UUID;

import eu.solven.kumite.account.KumiteUser;
import eu.solven.kumite.account.KumiteUserRaw;
import eu.solven.kumite.account.KumiteUserRawRaw;
import eu.solven.kumite.player.KumitePlayer;
import eu.solven.kumite.user.IKumiteUserRawRawRepository;
import eu.solven.kumite.user.IKumiteUserRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class KumiteUsersRegistry {

	// This is a cache of the external information about a user
	// This is useful to enrich some data about other players (e.g. a Leaderboard)
	// final Map<KumiteUserRawRaw, KumiteUser> userIdToUser = new ConcurrentHashMap<>();
	final IKumiteUserRepository userRepository;

	// We may have multiple users for a single account
	// This maps to the latest/main one
	// final Map<UUID, KumiteUserRawRaw> accountIdToUser = new ConcurrentHashMap<>();
	final IKumiteUserRawRawRepository userRawRawRepository;

	public KumiteUser getUser(UUID accountId) {
		KumiteUserRawRaw rawUser = userRawRawRepository.getUser(accountId)
				.orElseThrow(() -> new IllegalArgumentException("No accountId=" + accountId));

		return getUser(rawUser);
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
	public KumiteUser registerOrUpdate(KumiteUserRaw kumiteUserRaw) {
		KumiteUser kumiteUser;

		// `synchronized` to pack write on userIdToUser and accountIdToUser
		synchronized (this) {
			kumiteUser = userRepository.registerOrUpdate(kumiteUserRaw);
		}

		return kumiteUser;
	}

	public KumitePlayer getAccountMainPlayer(UUID accountId) {
		KumiteUser user = getUser(accountId);
		return KumitePlayer.builder().playerId(user.getPlayerId()).accountId(accountId).build();
	}

}
