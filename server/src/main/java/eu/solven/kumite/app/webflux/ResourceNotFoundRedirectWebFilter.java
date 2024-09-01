package eu.solven.kumite.app.webflux;

import java.net.URI;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

/**
 * This is useful for SinglePageApplication with HTML5 history mode. As the browser route is meaning from the API
 * point-of-view, we want to direct each browser route to the root/index.html
 */
// https://stackoverflow.com/questions/61822027/how-to-forward-to-on-404-error-in-webflux
@Order(-2)
@Component
public class ResourceNotFoundRedirectWebFilter implements WebFilter {
	final URI rootUri = URI.create("/");

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		if (exchange.getResponse().getStatusCode() == HttpStatus.NOT_FOUND) {
			exchange.getResponse().setStatusCode(HttpStatus.PERMANENT_REDIRECT);
			exchange.getResponse().getHeaders().setLocation(rootUri);
			return exchange.getResponse().setComplete();
		}
		return chain.filter(exchange);
	}
}