package eu.solven.kumite.app;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.session.ReactiveMapSessionRepository;
import org.springframework.session.ReactiveSessionRepository;

import eu.solven.kumite.app.webflux.KumiteWebFluxConfiguration;
import eu.solven.kumite.security.KumiteSecurity;
import eu.solven.kumite.tools.GitPropertySourceConfig;

@SpringBootApplication(scanBasePackages = "none")
@Import({

		KumiteWebFluxConfiguration.class,
		KumiteServerComponentsConfiguration.class,
		KumiteSecurity.class,
		GitPropertySourceConfig.class,

})
public class KumiteContestServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(KumiteContestServerApplication.class, args);
	}

	// https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide#spring-session-store-type
	@Bean
	// This will override any auto-configured SessionRepository like Redis one
	@Profile({ IKumiteSpringProfiles.P_INMEMORY })
	public ReactiveSessionRepository<?> inmemorySessionRepository() {
		return new ReactiveMapSessionRepository(new ConcurrentHashMap<>());
	}

}