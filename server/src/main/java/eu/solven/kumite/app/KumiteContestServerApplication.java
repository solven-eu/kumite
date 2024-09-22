package eu.solven.kumite.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

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

}
