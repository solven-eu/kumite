package eu.solven.kumite.app.it;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import eu.solven.kumite.app.KumiteServerComponentsConfiguration;
import eu.solven.kumite.app.webflux.KumiteWebFluxConfiguration;
import eu.solven.kumite.app.webflux.api.GreetingHandler;
import lombok.extern.slf4j.Slf4j;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = { KumiteWebFluxConfiguration.class, KumiteServerComponentsConfiguration.class, },
		webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Slf4j
public class TestKumiteRouterSpringConfig {

	@Test
	public void testHello() {
		log.debug("About {}", GreetingHandler.class);
	}
}