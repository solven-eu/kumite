package eu.solven.kumite.account;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import eu.solven.kumite.account.fake_player.FakePlayer;
import eu.solven.kumite.account.fake_player.FakeUser;
import eu.solven.kumite.account.fake_player.RandomPlayer;
import eu.solven.kumite.account.fake_player.RandomUser;
import eu.solven.kumite.account.internal.KumiteUser;
import eu.solven.kumite.account.internal.KumiteUserPreRegister;
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
	public KumiteUser registerOrUpdate(KumiteUserPreRegister kumiteUserPreRegister) {
		KumiteUserRawRaw rawRaw = kumiteUserPreRegister.getRawRaw();

		return accountIdToUser.compute(rawRaw, (k, alreadyIn) -> {
			KumiteUser.KumiteUserBuilder kumiteUserBuilder = KumiteUser.builder()
					.rawRaw(rawRaw)
					// TODO We should merge with pre-existing details
					.details(kumiteUserPreRegister.getDetails());
			if (alreadyIn == null) {
				UUID accountId = generateAccountId(rawRaw);

				KumitePlayer player = register(rawRaw, accountId);

				UUID playerId = player.getPlayerId();
				log.info("Registering as new user accountId={} playerId={} raw={}",
						accountId,
						playerId,
						kumiteUserPreRegister);

				kumiteUserBuilder.accountId(accountId).playerId(playerId);
			} else {
				kumiteUserBuilder.accountId(alreadyIn.getAccountId()).playerId(alreadyIn.getPlayerId());
			}

			return kumiteUserBuilder.build();
		});
	}

	protected UUID generateAccountId(KumiteUserRawRaw rawRaw) {
		return generateAccountId(uuidGenerator, rawRaw);
	}

	public static UUID generateAccountId(IUuidGenerator uuidGenerator, KumiteUserRawRaw rawRaw) {
		if (rawRaw.equals(FakeUser.rawRaw())) {
			return FakePlayer.ACCOUNT_ID;
		} else if (rawRaw.equals(RandomUser.rawRaw())) {
			return RandomPlayer.ACCOUNT_ID;
		}
		return uuidGenerator.randomUUID();
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
}
