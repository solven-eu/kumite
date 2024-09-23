package eu.solven.kumite.account;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.InitializingBean;

import eu.solven.kumite.account.fake_player.FakePlayerTokens;
import eu.solven.kumite.player.IAccountPlayersRegistry;
import eu.solven.kumite.player.KumitePlayer;
import eu.solven.kumite.tools.IUuidGenerator;
import eu.solven.kumite.tools.JdkUuidGenerator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public class InMemoryUserRepository implements IKumiteUserRepository, IKumiteUserRawRawRepository, InitializingBean {
	final Map<KumiteUserRawRaw, KumiteUser> accountIdToUser = new ConcurrentHashMap<>();
	final Map<UUID, KumiteUserRawRaw> accountIdToRawRaw = new ConcurrentHashMap<>();

	final IUuidGenerator uuidGenerator;

	final IAccountPlayersRegistry playersRegistry;

	@Override
	public void afterPropertiesSet() {
		KumiteUser fakeUser = FakePlayerTokens.fakeUser();
		KumiteUserRawRaw rawRaw = fakeUser.getRaw().getRawRaw();

		accountIdToRawRaw.put(fakeUser.getAccountId(), rawRaw);
		accountIdToUser.put(rawRaw, fakeUser);
	}

	@Override
	public Optional<KumiteUser> getUser(KumiteUserRawRaw accountId) {
		return Optional.ofNullable(accountIdToUser.get(accountId));
	}

	@Override
	public Optional<KumiteUserRawRaw> getUser(UUID userRawRaw) {
		return Optional.ofNullable(accountIdToRawRaw.get(userRawRaw));
	}

	@Override
	public void putIfAbsent(UUID accountId, KumiteUserRawRaw rawRaw) {
		accountIdToRawRaw.putIfAbsent(accountId, rawRaw);
	}

	@Override
	public KumiteUser registerOrUpdate(KumiteUserRaw kumiteUserRaw) {
		KumiteUserRawRaw rawRaw = kumiteUserRaw.getRawRaw();

		return accountIdToUser.compute(rawRaw, (k, alreadyIn) -> {
			KumiteUser.KumiteUserBuilder kumiteUserBuilder = KumiteUser.builder().raw(kumiteUserRaw);
			if (alreadyIn == null) {
				UUID accountId = uuidGenerator.randomUUID();

				KumitePlayer player = register(rawRaw, accountId);

				UUID playerId = player.getPlayerId();
				log.info("Registering as new user accountId={} playerId={} raw={}", accountId, playerId, kumiteUserRaw);

				kumiteUserBuilder.accountId(accountId).playerId(playerId);
			} else {
				kumiteUserBuilder.accountId(alreadyIn.getAccountId()).playerId(alreadyIn.getPlayerId());
			}

			return kumiteUserBuilder.build();
		});
	}

	private KumitePlayer register(KumiteUserRawRaw rawRaw, UUID accountId) {
		UUID playerId = playersRegistry.generateMainPlayerId(accountId);

		// In case of race-conditions, we would register unused accountId and playerIds: such dangling IDs
		// should be removed regularly
		KumitePlayer player = KumitePlayer.builder().playerId(playerId).accountId(accountId).build();
		playersRegistry.registerPlayer(player);
		putIfAbsent(accountId, rawRaw);

		return player;
	}

	public static InMemoryUserRepository forTests(IAccountPlayersRegistry playersRegistry) {
		InMemoryUserRepository inMemoryUserRepository =
				new InMemoryUserRepository(JdkUuidGenerator.INSTANCE, playersRegistry);
		inMemoryUserRepository.afterPropertiesSet();
		return inMemoryUserRepository;
	}

}
