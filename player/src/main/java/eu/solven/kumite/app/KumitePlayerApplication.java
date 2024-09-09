package eu.solven.kumite.app;

import org.apache.logging.log4j.util.Strings;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({ KumitePlayerComponentsConfiguration.class })
public class KumitePlayerApplication {

	public static void main(String[] args) {
		if (Strings.isEmpty(System.getProperty("spring.profiles.active"))) {
			System.setProperty("spring.profiles.active", "default");
		}

		SpringApplication.run(KumitePlayerApplication.class, args);
	}

}
