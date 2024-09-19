package eu.solven.kumite.app.webflux;

import org.springframework.context.annotation.Import;

import eu.solven.kumite.app.greeting.GreetingHandler;
import eu.solven.kumite.contest.ContestSearchHandler;
import eu.solven.kumite.game.GameSearchHandler;
import eu.solven.kumite.leaderboard.LeaderboardHandler;
import eu.solven.kumite.webhook.WebhooksHandler;
import lombok.extern.slf4j.Slf4j;

@Import({

		KumiteApiRouter.class,
		KumiteSpaRouter.class,
		KumiteFakeUserRouter.class,

		GreetingHandler.class,
		GameSearchHandler.class,
		ContestSearchHandler.class,
		LeaderboardHandler.class,
		WebhooksHandler.class,

		KumiteLoginRouter.class,
		AccessTokenHandler.class,

})
@Slf4j
public class KumiteWebFluxConfiguration {

}
