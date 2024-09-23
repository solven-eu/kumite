package eu.solven.kumite.user;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import eu.solven.kumite.account.InMemoryUserRepository;
import eu.solven.kumite.account.KumiteUserRawRaw;
import eu.solven.kumite.account.fake_player.FakePlayerTokens;
import eu.solven.kumite.player.persistence.BijectiveAccountPlayersRegistry;

public class TestInMemoryUserRepository {
	BijectiveAccountPlayersRegistry playersRegistry = new BijectiveAccountPlayersRegistry();

	@Test
	public void testFakeAccount() {
		InMemoryUserRepository userRepository = InMemoryUserRepository.forTests(playersRegistry);

		Optional<KumiteUserRawRaw> optRawRaw = userRepository.getUser(FakePlayerTokens.FAKE_ACCOUNT_ID);

		Assertions.assertThat(optRawRaw).isPresent();

	}
}
