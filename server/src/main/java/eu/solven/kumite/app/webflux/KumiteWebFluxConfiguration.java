package eu.solven.kumite.app.webflux;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.server.WebFilter;

import eu.solven.kumite.app.greeting.GreetingHandler;
import eu.solven.kumite.contest.ContestSearchHandler;
import eu.solven.kumite.game.GameSearchHandler;
import eu.solven.kumite.leaderboard.LeaderboardHandler;
import eu.solven.kumite.webhook.WebhooksHandler;
import lombok.extern.slf4j.Slf4j;

@Import({ KumiteRouter.class,

		GreetingHandler.class,
		GameSearchHandler.class,
		ContestSearchHandler.class,
		LeaderboardHandler.class,
		WebhooksHandler.class,

})
@Slf4j
public class KumiteWebFluxConfiguration {

	@Bean
	WebFilter kumiteExceptionRoutingWebFilter() {
		return new KumiteExceptionRoutingWebFilter();
	}
}
