package eu.solven.kumite.app;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import eu.solven.kumite.account.KumiteUsersRegistry;
import eu.solven.kumite.account.fake_player.FakePlayer;
import eu.solven.kumite.account.fake_player.RandomPlayer;
import eu.solven.kumite.player.IAccountPlayersRegistry;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class InjectKumiteAccountsConfig {

	@Profile(IKumiteSpringProfiles.P_FAKEUSER)
	@Bean
	public Void initFakePlayer(KumiteUsersRegistry usersRegistry, IAccountPlayersRegistry accountPlayersRegistry) {
		log.info("Registering the {} account and players", IKumiteSpringProfiles.P_FAKEUSER);

		usersRegistry.registerOrUpdate(FakePlayer.user().getRaw());
		accountPlayersRegistry.registerPlayer(FakePlayer.fakePlayer(1));

		return null;
	}

	@Bean
	public Void initRandomPlayer(KumiteUsersRegistry usersRegistry, IAccountPlayersRegistry accountPlayersRegistry) {
		log.info("Registering the random account and players");

		usersRegistry.registerOrUpdate(RandomPlayer.user().getRaw());
		accountPlayersRegistry.registerPlayer(RandomPlayer.randomPlayer(1));

		return null;
	}
}
