package eu.solven.kumite.user;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;

import eu.solven.kumite.account.IKumiteUserRawRawRepository;
import eu.solven.kumite.account.IKumiteUserRepository;
import eu.solven.kumite.account.KumiteUser;
import eu.solven.kumite.account.KumiteUserRaw;
import eu.solven.kumite.account.KumiteUserRawRaw;
import eu.solven.kumite.account.fake_player.FakePlayerTokens;
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

	@Override
	public Optional<KumiteUser> getUser(KumiteUserRawRaw userRawRaw) {
		KumiteUser kumiteUser = (KumiteUser) valueOpRawRaw(userRawRaw).get();
		return Optional.ofNullable(kumiteUser);
	}

	@Override
	public KumiteUser registerOrUpdate(KumiteUserRaw kumiteUserRaw) {
		KumiteUserRawRaw rawRaw = kumiteUserRaw.getRawRaw();
		Optional<KumiteUser> user = getUser(rawRaw);

		if (user.isEmpty()) {
			UUID accountId = generateAccountId(kumiteUserRaw.getRawRaw());
			UUID playerId = playersRegistry.generateMainPlayerId(accountId);

			KumiteUser kumiteUser =
					KumiteUser.builder().accountId(accountId).playerId(playerId).raw(kumiteUserRaw).build();
			log.info("Registered user={}", user);
			putIfAbsent(accountId, rawRaw);
			putIfAbsent(rawRaw, kumiteUser);

			playersRegistry.registerPlayer(KumitePlayer.builder().playerId(playerId).accountId(accountId).build());
		}

		return getUser(rawRaw).orElseThrow(() -> new IllegalStateException("No user through we just registered one"));
	}

	private UUID generateAccountId(KumiteUserRawRaw rawRaw) {
		if (rawRaw.equals(FakePlayerTokens.fakeUser().getRaw().getRawRaw())) {
			return FakePlayerTokens.FAKE_ACCOUNT_ID;
		}
		return uuidGenerator.randomUUID();
	}

}
