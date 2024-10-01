package eu.solven.kumite.user;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;

import eu.solven.kumite.account.IKumiteUserRawRawRepository;
import eu.solven.kumite.account.IKumiteUserRepository;
import eu.solven.kumite.account.InMemoryUserRepository;
import eu.solven.kumite.account.KumiteUserDetails;
import eu.solven.kumite.account.KumiteUserRawRaw;
import eu.solven.kumite.account.internal.KumiteUser;
import eu.solven.kumite.account.internal.KumiteUserPreRegister;
import eu.solven.kumite.player.IAccountPlayersRegistry;
import eu.solven.kumite.player.KumitePlayer;
import eu.solven.kumite.redis.RepositoryKey;
import eu.solven.kumite.tools.IUuidGenerator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * A {@link IKumiteUserRawRawRepository} and {@link IKumiteUserRepository} based on Redis.
 * 
 * The key is the accountId.
 * 
 * @author Benoit Lacelle
 *
 */
@AllArgsConstructor
@Slf4j
public class RedisUserRepository implements IKumiteUserRawRawRepository, IKumiteUserRepository {
	final RedisTemplate<Object, Object> redisTemplate;

	final IUuidGenerator uuidGenerator;

	final IAccountPlayersRegistry playersRegistry;

	// No TTL on Users
	private RepositoryKey<UUID> key(UUID accountId) {
		return RepositoryKey.<UUID>builder()
				.storeName(KumiteUserRawRaw.class.getSimpleName())
				.actualKey(accountId)
				.build();
	}

	private BoundValueOperations<Object, Object> valueOp(UUID accountId) {
		return redisTemplate.boundValueOps(key(accountId));
	}

	@Override
	public void putIfAbsent(UUID accountId, KumiteUserRawRaw kumiteUserRawRaw) {
		Boolean result = valueOp(accountId).setIfAbsent(kumiteUserRawRaw);
		log.info("accountId={} rawRaw={} putIfAbsent={}", accountId, kumiteUserRawRaw, result);
	}

	@Override
	public Optional<KumiteUserRawRaw> getUser(UUID accountId) {
		KumiteUserRawRaw userRawRaw = (KumiteUserRawRaw) valueOp(accountId).get();
		return Optional.ofNullable(userRawRaw);
	}

	private RepositoryKey<KumiteUserRawRaw> rawRawKey(KumiteUserRawRaw rawRaw) {
		return RepositoryKey.<KumiteUserRawRaw>builder()
				.storeName(KumiteUser.class.getSimpleName())
				.actualKey(rawRaw)
				.build();
	}

	private BoundValueOperations<Object, Object> valueOpRawRaw(KumiteUserRawRaw rawRaw) {
		return redisTemplate.boundValueOps(rawRawKey(rawRaw));
	}

	void putIfAbsent(KumiteUserRawRaw userRawRaw, KumiteUser kumiteUser) {
		Boolean result = valueOpRawRaw(userRawRaw).setIfAbsent(kumiteUser);
		log.info("rawRaw={} user={} putIfAbsent={}", userRawRaw, kumiteUser, result);
	}

	void putIfPresent(KumiteUserRawRaw userRawRaw, KumiteUser kumiteUser) {
		Boolean result = valueOpRawRaw(userRawRaw).setIfPresent(kumiteUser);
		log.info("rawRaw={} user={} putIfAbsent={}", userRawRaw, kumiteUser, result);
	}

	@Override
	public Optional<KumiteUser> getUser(KumiteUserRawRaw userRawRaw) {
		KumiteUser kumiteUser = (KumiteUser) valueOpRawRaw(userRawRaw).get();
		return Optional.ofNullable(kumiteUser);
	}

	@Override
	public KumiteUser registerOrUpdate(KumiteUserPreRegister kumiteUserPreRegister) {
		KumiteUserRawRaw rawRaw = kumiteUserPreRegister.getRawRaw();
		KumiteUserDetails userDetails = kumiteUserPreRegister.getDetails();
		Optional<KumiteUser> user = getUser(rawRaw);

		if (user.isEmpty()) {
			UUID accountId = generateAccountId(rawRaw);
			UUID playerId = playersRegistry.generateMainPlayerId(accountId);

			KumiteUser kumiteUser = KumiteUser.builder()
					.accountId(accountId)
					.playerId(playerId)
					.rawRaw(rawRaw)
					.details(userDetails)
					.build();
			log.info("Registered user={}", user);
			putIfAbsent(accountId, rawRaw);
			putIfAbsent(rawRaw, kumiteUser);

			playersRegistry.registerPlayer(KumitePlayer.builder().playerId(playerId).accountId(accountId).build());
		} else {
			// Merge the raw, especially as it may be fed from different sources (username from OAuth2, countryCode from
			// browser)

			KumiteUserDetails previousDetails = user.get().getDetails();

			if (userDetails.getCountryCode() == null) {
				userDetails = userDetails.setCountryCode(previousDetails.getCountryCode());
			}
			if (userDetails.getCompany() == null) {
				userDetails = userDetails.setCompany(previousDetails.getCompany());
			}
			if (userDetails.getSchool() == null) {
				userDetails = userDetails.setSchool(previousDetails.getSchool());
			}

			putIfPresent(rawRaw, user.get().editDetails(userDetails));
		}

		return getUser(rawRaw).orElseThrow(() -> new IllegalStateException("No user through we just registered one"));
	}

	protected UUID generateAccountId(KumiteUserRawRaw rawRaw) {
		return InMemoryUserRepository.generateAccountId(uuidGenerator, rawRaw);
	}

}
