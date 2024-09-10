package eu.solven.kumite.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({ KumiteServerComponentsConfiguration.class, KumitePlayerComponentsConfiguration.class })
public class KumiteMonolithApplication {

	public static void main(String[] args) {
		SpringApplication.run(KumiteMonolithApplication.class, args);
	}

}
