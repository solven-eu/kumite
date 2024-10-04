package eu.solven.kumite.websocket;

import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.WebSocketService;
import org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService;
import org.springframework.web.reactive.socket.server.upgrade.TomcatRequestUpgradeStrategy;
import org.springframework.web.server.ServerWebExchange;

import eu.solven.kumite.account.KumiteUsersRegistry;
import eu.solven.kumite.account.internal.KumiteUser;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Deprecated(since = "Unclear when this would be nedded as SpringSecurity seems to trigger-in naturally")
@EnableWebFlux
@AllArgsConstructor
@Slf4j
public class KumiteWebSocketManualAuthSpringConfig implements WebFluxConfigurer {

	final ReactiveJwtDecoder jwtDecoder;
	final KumiteUsersRegistry usersRegistry;

	// https://docs.spring.io/spring-framework/reference/web/webflux-websocket.html
	// https://docs.spring.io/spring-framework/reference/web/webflux/config.html#webflux-config-websocket-service
	@Override
	public WebSocketService getWebSocketService() {
		TomcatRequestUpgradeStrategy strategy = new TomcatRequestUpgradeStrategy();
		strategy.setMaxSessionIdleTimeout(0L);
		return new HandshakeWebSocketService(strategy) {
			@Override
			public Mono<Void> handleRequest(ServerWebExchange exchange, WebSocketHandler handler) {
				String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
				if (authHeader == null) {
					exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
					return exchange.getResponse().setComplete();
				} else if (!authHeader.startsWith("Bearer ")) {
					log.info("{} header not starting with {}: {}", HttpHeaders.AUTHORIZATION, "Bearer ", authHeader);
					exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
					return exchange.getResponse().setComplete();
				} else {
					String token = authHeader.substring("Bearer ".length());

					Mono<Jwt> jwtMono = jwtDecoder.decode(token);

					return jwtMono.doOnNext(jwt -> {
						KumiteUser user = usersRegistry.getUser(UUID.fromString(jwt.getSubject()));

						log.info("A webSocket CONNECT as {}", user.getAccountId());
					}).flatMap(jwt -> {
						return super.handleRequest(exchange, handler);
					});
				}

			}
		};
	}
}
