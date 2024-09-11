package eu.solven.kumite.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import eu.solven.kumite.account.login.KumiteSecurity;
import eu.solven.kumite.app.webflux.KumiteWebFluxConfiguration;

@SpringBootApplication(scanBasePackages = "none")
@Import({ KumiteWebFluxConfiguration.class, KumiteServerComponentsConfiguration.class, KumiteSecurity.class })
public class KumiteServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(KumiteServerApplication.class, args);
	}

}
