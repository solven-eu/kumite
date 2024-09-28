package eu.solven.kumite.app.webflux;

import org.springframework.context.annotation.Import;

import eu.solven.kumite.app.webflux.api.AccessTokenHandler;
import eu.solven.kumite.app.webflux.api.GreetingHandler;
import eu.solven.kumite.app.webflux.api.KumiteApiRouter;
import eu.solven.kumite.app.webflux.api.KumiteFakeUserRouter;
import eu.solven.kumite.app.webflux.api.KumiteLoginRouter;
import eu.solven.kumite.app.webflux.api.KumiteSpaRouter;
import eu.solven.kumite.board.BoardHandler;
import eu.solven.kumite.contest.ContestHandler;
import eu.solven.kumite.game.GameSearchHandler;
import eu.solven.kumite.leaderboard.LeaderboardHandler;
import eu.solven.kumite.oauth2.authorizationserver.ActiveRefreshTokens;
import eu.solven.kumite.oauth2.authorizationserver.KumiteTokenService;
import eu.solven.kumite.player.PlayerMovesHandler;
import eu.solven.kumite.player.PlayersSearchHandler;
import eu.solven.kumite.webhook.WebhooksHandler;
import lombok.extern.slf4j.Slf4j;

@Import({

		KumiteApiRouter.class,
		KumiteSpaRouter.class,
		KumiteFakeUserRouter.class,

		GreetingHandler.class,
		GameSearchHandler.class,
		ContestHandler.class,
		LeaderboardHandler.class,
		WebhooksHandler.class,

		PlayersSearchHandler.class,
		BoardHandler.class,
		PlayerMovesHandler.class,

		KumiteLoginRouter.class,
		AccessTokenHandler.class,
		ActiveRefreshTokens.class,

		// The contest-server generate its own RefreshTokens and AccessTokens (hence it acts as its own
		// AuthroizationServer)
		KumiteTokenService.class,

})
@Slf4j
public class KumiteWebFluxConfiguration {

}
