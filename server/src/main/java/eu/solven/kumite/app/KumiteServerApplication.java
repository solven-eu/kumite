package eu.solven.kumite.app;

import org.apache.logging.log4j.util.Strings;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import eu.solven.kumite.account.login.KumiteSecurity;
import eu.solven.kumite.app.webflux.KumiteWebFluxConfiguration;

@SpringBootApplication
@Import({ KumiteWebFluxConfiguration.class, KumiteServerComponentsConfiguration.class, KumiteSecurity.class })
public class KumiteServerApplication {

	public static void main(String[] args) {
		if (Strings.isEmpty(System.getProperty("spring.profiles.active"))) {
			System.setProperty("spring.profiles.active", "default,inject_default_games,fake_user");
		}

		SpringApplication.run(KumiteServerApplication.class, args);
	}

}
