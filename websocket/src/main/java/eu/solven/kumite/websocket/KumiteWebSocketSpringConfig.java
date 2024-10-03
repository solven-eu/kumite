package eu.solven.kumite.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

import eu.solven.kumite.account.KumiteUsersRegistry;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Import({

		WebSocketSecurityConfig.class,

		KumiteContestEventsWebSocketHandler.class,
		KumiteWebsocketHandlerMapping.class,

		ContestsEventPublisher.class,

})
//@EnableWebFlux
@AllArgsConstructor
@Slf4j
public class KumiteWebSocketSpringConfig 
//implements WebFluxConfigurer 
{

	final ReactiveJwtDecoder jwtDecoder;
	final KumiteUsersRegistry usersRegistry;

	// https://docs.spring.io/spring-framework/reference/web/webflux-websocket.html
	// https://docs.spring.io/spring-framework/reference/web/webflux/config.html#webflux-config-websocket-service
//	@Override
//	public WebSocketService getWebSocketService() {
//		TomcatRequestUpgradeStrategy strategy = new TomcatRequestUpgradeStrategy();
//		strategy.setMaxSessionIdleTimeout(0L);
//		return new HandshakeWebSocketService(strategy) {
//			@Override
//			public Mono<Void> handleRequest(ServerWebExchange exchange, WebSocketHandler handler) {
////				String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
////				if (authHeader == null) {
////					exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
////					return exchange.getResponse().setComplete();
////				} else if (!authHeader.startsWith("Bearer ")) {
////					log.info("{} header not starting with {}: {}", HttpHeaders.AUTHORIZATION, "Bearer ", authHeader);
////					exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
////					return exchange.getResponse().setComplete();
////				} else {
////					String token = authHeader.substring("Bearer ".length());
////
////					Mono<Jwt> jwtMono = jwtDecoder.decode(token);
////
////					return jwtMono.doOnNext(jwt -> {
////						KumiteUser user = usersRegistry.getUser(UUID.fromString(jwt.getSubject()));
////
////						log.info("A webSocket CONNECT as {}", user.getAccountId());
////					}).flatMap(jwt -> {
//						return super.handleRequest(exchange, handler);
////					});
////				}
//
//			}
//		};
//	}
}
