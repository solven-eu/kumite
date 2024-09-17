package eu.solven.kumite.user;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import eu.solven.kumite.account.KumiteUser;
import eu.solven.kumite.account.KumiteUserRaw;
import eu.solven.kumite.account.KumiteUserRawRaw;
import eu.solven.kumite.player.IAccountPlayersRegistry;
import eu.solven.kumite.player.KumitePlayer;
import eu.solven.kumite.tools.IUuidGenerator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public class InMemoryUserRepository implements IKumiteUserRepository, IKumiteUserRawRawRepository {
	final Map<KumiteUserRawRaw, KumiteUser> accountIdToUser = new ConcurrentHashMap<>();
	final Map<UUID, KumiteUserRawRaw> accountIdToRawRaw = new ConcurrentHashMap<>();

	final IUuidGenerator uuidGenerator;

	final IAccountPlayersRegistry playersRegistry;

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
				kumiteUserBuilder.accountId(accountId);

				UUID playerId = playersRegistry.generateMainPlayerId(accountId);
				kumiteUserBuilder.playerId(playerId);

				log.info("Registering as new user accountId={} playerId={} raw={}", accountId, playerId, kumiteUserRaw);

				// In case of race-conditions, we would register unused accountId and playerIds: such dangling IDs
				// should be removed regularly
				playersRegistry.registerPlayer(accountId, KumitePlayer.builder().playerId(playerId).build());
				putIfAbsent(accountId, rawRaw);
			} else {
				kumiteUserBuilder.accountId(alreadyIn.getAccountId()).playerId(alreadyIn.getPlayerId());
			}

			return kumiteUserBuilder.build();
		});
	}

}
