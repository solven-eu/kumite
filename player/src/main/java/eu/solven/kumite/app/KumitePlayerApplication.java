package eu.solven.kumite.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({ KumitePlayerComponentsConfiguration.class })
public class KumitePlayerApplication {

	public static void main(String[] args) {
		SpringApplication.run(KumitePlayerApplication.class, args);
	}

}
