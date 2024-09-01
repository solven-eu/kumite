package eu.solven.kumite.app.webflux;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.WebFilter;

import eu.solven.kumite.app.greeting.GreetingHandler;
import eu.solven.kumite.contest.ContestSearchHandler;
import eu.solven.kumite.game.GameSearchHandler;
import eu.solven.kumite.leaderboard.LeaderboardHandler;
import eu.solven.kumite.webhook.WebhooksHandler;

@Import({ KumiteRouter.class,

		GreetingHandler.class,
		GameSearchHandler.class,
		ContestSearchHandler.class,
		LeaderboardHandler.class,
		WebhooksHandler.class,

})
public class KumiteWebFluxConfiguration {

	// https://stackoverflow.com/questions/51931178/error-handling-in-webflux-with-routerfunction
	@Bean
	WebFilter dataNotFoundToBadRequest() {
		return (exchange, next) -> next.filter(exchange).onErrorResume(IllegalArgumentException.class, e -> {
			ServerHttpResponse response = exchange.getResponse();
			response.setStatusCode(HttpStatus.BAD_REQUEST);
			return response.setComplete();
		});
	}
}
