package eu.solven.kumite.account.login;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import eu.solven.kumite.account.KumiteUser;
import eu.solven.kumite.account.KumiteUser.KumiteUserBuilder;
import eu.solven.kumite.account.KumiteUserRaw;
import eu.solven.kumite.account.KumiteUserRawRaw;
import eu.solven.kumite.player.AccountPlayersRegistry;
import eu.solven.kumite.player.KumitePlayer;
import eu.solven.kumite.tools.IUuidGenerator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class KumiteUsersRegistry {
	final IUuidGenerator uuidGenerator;

	final AccountPlayersRegistry playersRegistry;

	// This is a cache of the external information about a user
	// This is useful to enrich some data about other players (e.g. a Leaderboard)
	final Map<KumiteUserRawRaw, KumiteUser> userIdToUser = new ConcurrentHashMap<>();

	// We may have multiple users for a single account
	// This maps to the latest/main one
	final Map<UUID, KumiteUserRawRaw> accountIdToUser = new ConcurrentHashMap<>();

	public KumiteUser getUser(UUID accountId) {
		KumiteUserRawRaw rawUser = accountIdToUser.get(accountId);

		if (rawUser == null) {
			throw new IllegalArgumentException("No accountId=" + accountId);
		}

		return userIdToUser.get(rawUser);
	}

	public KumiteUser getUser(KumiteUserRawRaw rawUser) {
		KumiteUser user = userIdToUser.get(rawUser);
		if (user == null) {
			throw new IllegalArgumentException("No existing user matching " + rawUser);
		}
		return user;
	}

	/**
	 * 
	 * @param kumiteUserRaw
	 * @return a {@link KumiteUser}. This may be a new account if this was not known. If this was already known, we
	 *         update the oauth2 details and return an existing accountId
	 */
	public KumiteUser registerOrUpdate(KumiteUserRaw kumiteUserRaw) {
		KumiteUserRawRaw rawRaw = kumiteUserRaw.getRawRaw();

		KumiteUser kumiteUser;

		// `synchronized` to pack write on userIdToUser and accountIdToUser
		synchronized (this) {
			kumiteUser = userIdToUser.compute(rawRaw, (k, alreadyIn) -> {
				KumiteUserBuilder kumiteUserBuilder = KumiteUser.builder().raw(kumiteUserRaw);
				if (alreadyIn == null) {
					UUID accountId = uuidGenerator.randomUUID();
					kumiteUserBuilder.accountId(accountId);

					UUID playerId = uuidGenerator.randomUUID();
					kumiteUserBuilder.playerId(playerId);

					playersRegistry.registerPlayer(accountId, KumitePlayer.builder().playerId(playerId).build());
					accountIdToUser.putIfAbsent(accountId, rawRaw);
				} else {
					kumiteUserBuilder.accountId(alreadyIn.getAccountId()).playerId(alreadyIn.getPlayerId());
				}

				return kumiteUserBuilder.build();
			});
		}

		return kumiteUser;
	}

	public KumitePlayer getAccountMainPlayer(UUID accountId) {
		return KumitePlayer.builder().playerId(getUser(accountId).getPlayerId()).build();
	}

}
