package eu.solven.kumite.app;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import eu.solven.kumite.account.KumiteUser;
import eu.solven.kumite.account.KumiteUsersRegistry;
import eu.solven.kumite.account.fake_player.FakePlayer;
import eu.solven.kumite.account.fake_player.RandomPlayer;
import eu.solven.kumite.player.IAccountPlayersRegistry;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class InjectKumiteAccountsConfig {

	@Profile(IKumiteSpringProfiles.P_FAKEUSER)
	@Qualifier(IKumiteSpringProfiles.P_FAKEUSER)
	@Bean
	public KumiteUser initFakePlayer(KumiteUsersRegistry usersRegistry,
			IAccountPlayersRegistry accountPlayersRegistry) {
		log.info("Registering the {} account and players", IKumiteSpringProfiles.P_FAKEUSER);

		KumiteUser user = usersRegistry.registerOrUpdate(FakePlayer.user().getRaw());
		// Register an additional player
		accountPlayersRegistry.registerPlayer(FakePlayer.player(1));

		return user;
	}

	@Qualifier("random")
	@Bean
	public KumiteUser initRandomPlayer(KumiteUsersRegistry usersRegistry,
			IAccountPlayersRegistry accountPlayersRegistry) {
		log.info("Registering the random account and players");

		KumiteUser user = usersRegistry.registerOrUpdate(RandomPlayer.user().getRaw());
		// Register an additional player
		accountPlayersRegistry.registerPlayer(RandomPlayer.player(1));

		return user;
	}
}
