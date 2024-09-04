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

	// https://stackoverflow.com/questions/51931178/error-handling-in-webflux-with-routerfunction
	@Bean
	WebFilter dataNotFoundToBadRequest() {
		return (exchange, next) -> next.filter(exchange).onErrorResume(IllegalArgumentException.class, e -> {
			HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
			if (log.isDebugEnabled()) {
				log.warn("Returning a {} given {} ({})", httpStatus, e.getClass(), e.getMessage(), e);
			} else {
				log.warn("Returning a {} given {} ({})", httpStatus, e.getClass(), e.getMessage());
			}
			// TODO Add body with details
			ServerHttpResponse response = exchange.getResponse();
			response.setStatusCode(httpStatus);
			// Map<String, ?> body = new LinkedHashMap<>();
			// response.beforeCommit(() -> Mono.just(body));
			return response.setComplete();
		});
	}
}
