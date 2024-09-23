package eu.solven.kumite.app.it.security;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.session.ReactiveMapSessionRepository;
import org.springframework.session.ReactiveSessionRepository;

import eu.solven.kumite.account.InMemoryUserRepository;
import eu.solven.kumite.account.KumiteUsersRegistry;
import eu.solven.kumite.app.webflux.PlayerVerifierFilterFunction;
import eu.solven.kumite.app.webflux.api.AccessTokenHandler;
import eu.solven.kumite.app.webflux.api.GreetingHandler;
import eu.solven.kumite.app.webflux.api.KumiteLoginRouter;
import eu.solven.kumite.app.webflux.api.KumiteSpaRouter;
import eu.solven.kumite.player.persistence.BijectiveAccountPlayersRegistry;
import eu.solven.kumite.security.KumiteSecurity;
import eu.solven.kumite.tools.KumiteRandomConfiguration;

@SpringBootApplication(scanBasePackages = "none")
@Import({ KumiteRandomConfiguration.class,

		KumiteSecurity.class,

		KumiteUsersRegistry.class,
		InMemoryUserRepository.class,

		KumiteSpaRouter.class,
		GreetingHandler.class,

		KumiteLoginRouter.class,
		PlayerVerifierFilterFunction.class,
		AccessTokenHandler.class,

		// IAccountPlayersRegistry is needed as security often checks the players of an account
		BijectiveAccountPlayersRegistry.class,

})
public class KumiteServerSecurityApplication {

	public static void main(String[] args) {
		SpringApplication.run(KumiteServerSecurityApplication.class, args);
	}

	// https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide#spring-session-store-type
	@Bean
	public ReactiveSessionRepository<?> inmemorySessionRepository() {
		return new ReactiveMapSessionRepository(new ConcurrentHashMap<>());
	}

}
