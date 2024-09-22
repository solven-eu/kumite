package eu.solven.kumite.app.webflux;

import org.springframework.context.annotation.Import;

import eu.solven.kumite.app.webflux.api.AccessTokenHandler;
import eu.solven.kumite.app.webflux.api.GreetingHandler;
import eu.solven.kumite.app.webflux.api.KumiteApiRouter;
import eu.solven.kumite.app.webflux.api.KumiteFakeUserRouter;
import eu.solven.kumite.app.webflux.api.KumiteLoginRouter;
import eu.solven.kumite.app.webflux.api.KumiteSpaRouter;
import eu.solven.kumite.contest.ContestSearchHandler;
import eu.solven.kumite.game.GameSearchHandler;
import eu.solven.kumite.leaderboard.LeaderboardHandler;
import eu.solven.kumite.oauth2.authorizationserver.KumiteTokenService;
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

		// The contest-server generate its own RefreshTokens and AccessTokens (hence it acts as its own
		// AuthroizationServer)
		KumiteTokenService.class,

})
@Slf4j
public class KumiteWebFluxConfiguration {

}
