package eu.solven.kumite.app.webflux;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Convert an applicative {@link Throwable} into a relevant {@link HttpStatus}
 * 
 * @author Benoit Lacelle
 *
 */
// https://stackoverflow.com/questions/51931178/error-handling-in-webflux-with-routerfunction
@Slf4j
public class KumiteExceptionRoutingWebFilter implements WebFilter {

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		return chain.filter(exchange).onErrorResume(IllegalArgumentException.class, e -> {
			HttpStatus httpStatus = HttpStatus.BAD_REQUEST;

			if (log.isDebugEnabled()) {
				log.warn("Returning a {} given {} ({})", httpStatus, e.getClass(), e.getMessage(), e);
			} else {
				log.warn("Returning a {} given {} ({})", httpStatus, e.getClass(), e.getMessage());
			}

			ServerHttpResponse response = exchange.getResponse();
			response.setStatusCode(httpStatus);
			return response.setComplete();
		}).onErrorResume(LoginRouteButNotAuthenticatedException.class, e -> {
			// By default, OAuth2 would return a 302-FOUND to redirect to the `/login` URL
			// However, we prefer returning a 401
			HttpStatus httpStatus = HttpStatus.UNAUTHORIZED;

			if (log.isDebugEnabled()) {
				log.warn("Returning a {} given {} ({})", httpStatus, e.getClass(), e.getMessage(), e);
			} else {
				log.warn("Returning a {} given {} ({})", httpStatus, e.getClass(), e.getMessage());
			}

			ServerHttpResponse response = exchange.getResponse();
			response.setStatusCode(httpStatus);
			return response.setComplete();
		});
	}
}
