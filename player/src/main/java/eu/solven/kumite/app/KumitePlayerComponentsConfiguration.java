package eu.solven.kumite.app;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

@Configuration
@Import({

		KumiteRandomConfiguration.class,

})
public class KumitePlayerComponentsConfiguration {

	@Bean
	public IKumiteServer kumiteServer(Environment env) {
		return new KumiteWebclientServer(env);
	}

}
