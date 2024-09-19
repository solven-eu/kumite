package eu.solven.kumite.user;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.redis.core.RedisTemplate;

import eu.solven.kumite.account.KumiteUser;
import eu.solven.kumite.account.KumiteUserRaw;
import eu.solven.kumite.account.KumiteUserRawRaw;
import eu.solven.kumite.player.IAccountPlayersRegistry;
import eu.solven.kumite.player.KumitePlayer;
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

	@Override
	public void putIfAbsent(UUID accountId, KumiteUserRawRaw kumiteUserRawRaw) {
		Boolean result = redisTemplate.boundValueOps(kumiteUserRawRaw).setIfAbsent(kumiteUserRawRaw);
		log.info("accountId={} rawRaw={} putIfAbsent={}", accountId, kumiteUserRawRaw, result);
	}

	@Override
	public Optional<KumiteUserRawRaw> getUser(UUID accountId) {
		KumiteUserRawRaw userRawRaw = (KumiteUserRawRaw) redisTemplate.boundValueOps(accountId).get();
		return Optional.ofNullable(userRawRaw);
	}

	void putIfAbsent(KumiteUserRaw kumiteUserRaw, KumiteUser kumiteUser) {
		Boolean result = redisTemplate.boundValueOps(kumiteUserRaw.getRawRaw()).setIfAbsent(kumiteUser);
		log.info("raw={} user={} putIfAbsent={}", kumiteUserRaw, kumiteUser, result);
	}

	@Override
	public KumiteUser registerOrUpdate(KumiteUserRaw kumiteUserRaw) {
		KumiteUserRawRaw rawRaw = kumiteUserRaw.getRawRaw();
		Optional<KumiteUser> user = getUser(rawRaw);

		if (user.isEmpty()) {
			UUID accountId = uuidGenerator.randomUUID();
			UUID playerId = playersRegistry.generateMainPlayerId(accountId);

			KumiteUser kumiteUser =
					KumiteUser.builder().accountId(accountId).playerId(playerId).raw(kumiteUserRaw).build();
			log.info("Registered user={}", user);
			putIfAbsent(accountId, rawRaw);
			putIfAbsent(kumiteUserRaw, kumiteUser);

			playersRegistry.registerPlayer(KumitePlayer.builder().playerId(playerId).accountId(accountId).build());
		}

		return getUser(kumiteUserRaw.getRawRaw())
				.orElseThrow(() -> new IllegalStateException("No user through we just registered one"));
	}

	@Override
	public Optional<KumiteUser> getUser(KumiteUserRawRaw userRawRaw) {
		KumiteUser kumiteUser = (KumiteUser) redisTemplate.boundValueOps(userRawRaw).get();
		return Optional.ofNullable(kumiteUser);
	}

}
