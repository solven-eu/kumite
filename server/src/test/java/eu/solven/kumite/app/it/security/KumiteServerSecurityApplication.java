package eu.solven.kumite.app.it.security;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import eu.solven.kumite.account.login.KumiteSecurity;
import eu.solven.kumite.account.login.KumiteUsersRegistry;
import eu.solven.kumite.app.KumiteRandomConfiguration;
import eu.solven.kumite.app.greeting.GreetingHandler;
import eu.solven.kumite.app.webflux.KumiteSpaRouter;
import eu.solven.kumite.player.persistence.BijectiveAccountPlayersRegistry;
import eu.solven.kumite.user.InMemoryUserRepository;

@SpringBootApplication(scanBasePackages = "none")
@Import({ KumiteRandomConfiguration.class,

		KumiteSecurity.class,

		KumiteUsersRegistry.class,
		InMemoryUserRepository.class,

		KumiteSpaRouter.class,
		GreetingHandler.class,

		// IAccountPlayersRegistry is needed as security often checks the players of an account
		BijectiveAccountPlayersRegistry.class,

})
public class KumiteServerSecurityApplication {

	public static void main(String[] args) {
		SpringApplication.run(KumiteServerSecurityApplication.class, args);
	}

}
