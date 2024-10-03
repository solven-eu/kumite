package eu.solven.kumite.security;

import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.web.server.BearerTokenServerAuthenticationEntryPoint;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.savedrequest.NoOpServerRequestCache;

import com.nimbusds.jwt.JWT;

import eu.solven.kumite.app.IKumiteSpringProfiles;
import eu.solven.kumite.oauth2.resourceserver.KumiteResourceServerConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class JwtWebFluxSecurity {

	/**
	 * 
	 * @param http
	 * @param env
	 * @param jwtDecoder
	 *            Knows how to check a {@link JWT}, and convert it into a {@link Jwt}. Typically provided from
	 *            {@link KumiteResourceServerConfiguration}
	 * @return
	 */
	@Order(Ordered.LOWEST_PRECEDENCE)
	@Bean
	public SecurityWebFilterChain configureApi(Environment env,
			ReactiveJwtDecoder jwtDecoder,
			ServerHttpSecurity http) {

		boolean isFakeUser = env.acceptsProfiles(Profiles.of(IKumiteSpringProfiles.P_FAKEUSER));
		if (isFakeUser) {
			log.warn("{}=true", IKumiteSpringProfiles.P_FAKEUSER);
		} else {
			log.info("{}=false", IKumiteSpringProfiles.P_FAKEUSER);
		}

		// We can disable CSRF as these routes are stateless, does not rely on any cookie/session, but on some JWT
		return http

				// https://www.baeldung.com/spring-security-csrf
				.csrf(csrf -> {
					log.info("CSRF is disbled in API as API has stateless auth");
					csrf.disable();
				})
				.cors(cors -> {
					log.info("CORS is disbled in API as API has stateless auth");
					cors.disable();
				})

				.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtDecoder(jwtDecoder)))

				// https://github.com/spring-projects/spring-security/issues/6552
				.requestCache(cache -> cache.requestCache(NoOpServerRequestCache.getInstance()))

				.authorizeExchange(auth -> auth
						// Actuator is partly public
						.pathMatchers("/actuator/health/readiness", "/actuator/health/liveness")
						.permitAll()
						// Swagger/OpenAPI is public
						.pathMatchers("/v3/api-docs/**")
						.permitAll()
						// public API is public
						.pathMatchers("/api/v1/public/**")
						.permitAll()

						// WebSocket: the authentication is done manually on the CONNECT frame
						.pathMatchers("/ws/**")
						.permitAll()

						// If fakeUser==true, we allow the reset route (for integration tests)
						.pathMatchers(isFakeUser ? "/api/v1/clear" : "nonono")
						.permitAll()

						// The rest needs to be authenticated
						.anyExchange()
						.authenticated())

				// Default OAuth2 behavior is to redirect to login pages
				// If not loged-in, we want to receive 401 and not 302 (which are good for UX)
				.exceptionHandling(e -> {
					BearerTokenServerAuthenticationEntryPoint authenticationEntryPoint =
							new BearerTokenServerAuthenticationEntryPoint();
					authenticationEntryPoint.setRealmName("Kumite Realm");
					e.authenticationEntryPoint(authenticationEntryPoint);
				})

				// .anonymous(a -> a.principal("AnonymousKarateka"))
				.build();
	}

}
