package eu.solven.kumite.user;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import eu.solven.kumite.account.InMemoryUserRepository;
import eu.solven.kumite.account.KumiteUserRawRaw;
import eu.solven.kumite.account.fake_player.FakePlayer;
import eu.solven.kumite.account.fake_player.FakeUser;
import eu.solven.kumite.account.fake_player.RandomPlayer;
import eu.solven.kumite.account.fake_player.RandomUser;
import eu.solven.kumite.account.internal.KumiteUser;
import eu.solven.kumite.account.login.IKumiteTestConstants;
import eu.solven.kumite.player.persistence.BijectiveAccountPlayersRegistry;
import eu.solven.kumite.tools.JdkUuidGenerator;

public class TestInMemoryUserRepository {
	BijectiveAccountPlayersRegistry playersRegistry = new BijectiveAccountPlayersRegistry();
	InMemoryUserRepository userRepository = new InMemoryUserRepository(JdkUuidGenerator.INSTANCE, playersRegistry);

	@Test
	public void testRegisterUser() {
		KumiteUser user = userRepository.registerOrUpdate(IKumiteTestConstants.userPreRegister());

		Optional<KumiteUserRawRaw> optRawRaw = userRepository.getUser(user.getAccountId());
		Assertions.assertThat(optRawRaw).isPresent().contains(user.getRawRaw());
	}

	@Test
	public void testFakeUser() {
		// Not present by default
		{
			Optional<KumiteUserRawRaw> optRawRaw = userRepository.getUser(FakePlayer.ACCOUNT_ID);
			Assertions.assertThat(optRawRaw).isEmpty();
		}

		KumiteUser user = userRepository.registerOrUpdate(FakeUser.pre());
		Assertions.assertThat(user.getAccountId()).isEqualTo(FakePlayer.ACCOUNT_ID);

		Optional<KumiteUserRawRaw> optRawRaw = userRepository.getUser(user.getAccountId());
		Assertions.assertThat(optRawRaw).isPresent().contains(user.getRawRaw());
	}

	@Test
	public void testRandomUser() {
		// Not present by default
		{
			Optional<KumiteUserRawRaw> optRawRaw = userRepository.getUser(RandomPlayer.ACCOUNT_ID);
			Assertions.assertThat(optRawRaw).isEmpty();
		}

		KumiteUser user = userRepository.registerOrUpdate(RandomUser.pre());
		Assertions.assertThat(user.getAccountId()).isEqualTo(RandomPlayer.ACCOUNT_ID);

		Optional<KumiteUserRawRaw> optRawRaw = userRepository.getUser(user.getAccountId());
		Assertions.assertThat(optRawRaw).isPresent().contains(user.getRawRaw());
	}
}
