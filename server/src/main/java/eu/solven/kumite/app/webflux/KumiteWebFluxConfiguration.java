package eu.solven.kumite.app.webflux;

import org.springframework.context.annotation.Import;

import eu.solven.kumite.contest.ContestSearchHandler;
import eu.solven.kumite.game.GameSearchHandler;
import eu.solven.kumite.greeting.GreetingHandler;

@Import({ KumiteRouter.class,

		GreetingHandler.class,
		GameSearchHandler.class,
		ContestSearchHandler.class })
public class KumiteWebFluxConfiguration {

}
