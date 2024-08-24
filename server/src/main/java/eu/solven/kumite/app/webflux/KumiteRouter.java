package eu.solven.kumite.app.webflux;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import eu.solven.kumite.contest.ContestSearchHandler;
import eu.solven.kumite.game.GameSearchHandler;
import eu.solven.kumite.greeting.GreetingHandler;

@Configuration(proxyBeanMethods = false)
public class KumiteRouter {

	@Bean
	public RouterFunction<ServerResponse> route(GreetingHandler greetingHandler,
			GameSearchHandler gamesSearchHandler,
			ContestSearchHandler contestSearchHandler) {
		return RouterFunctions
				.route(RequestPredicates.GET("/hello").and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
						greetingHandler::hello)
				.and(RouterFunctions.route(
						RequestPredicates.GET("/games").and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
						gamesSearchHandler::listGames))
				.and(RouterFunctions.route(
						RequestPredicates.GET("/contests").and(RequestPredicates.accept(MediaType.APPLICATION_JSON)),
						contestSearchHandler::listContests));
	}
}