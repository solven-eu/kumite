package eu.solven.kumite.account.login;

import java.text.ParseException;

import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.web.server.BearerTokenServerAuthenticationEntryPoint;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;

import eu.solven.kumite.app.IKumiteSpringProfiles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@EnableWebFluxSecurity
@Import({

		KumiteOAuth2UserService.class,

})
@RequiredArgsConstructor
@Slf4j
public class SocialWebFluxSecurity {
	// https://www.baeldung.com/spring-security-csrf
	public static final boolean DISABLE_CSRF_CORS = true;

	// https://github.com/ch4mpy/spring-addons/tree/master/samples/tutorials/resource-server_with_ui
	// https://stackoverflow.com/questions/74744901/default-401-instead-of-redirecting-for-oauth2-login-spring-security
	// `-1` as this has to be used in priority aver the API securityFilterChain
	@Order(Ordered.LOWEST_PRECEDENCE - 1)
	@Bean
	public SecurityWebFilterChain configureUi(ServerProperties serverProperties,
			ServerHttpSecurity http,
			Environment env) {
		boolean isSsl = serverProperties.getSsl() != null && serverProperties.getSsl().isEnabled();

		ReactiveAuthenticationManager ram = auth -> {
			throw new IllegalStateException();
		};

		boolean defaultFakeUser = env.acceptsProfiles(Profiles.of(IKumiteSpringProfiles.P_DEFAULT_FAKE_USER));
		if (defaultFakeUser) {
			log.warn("defaultFakeUser=true");
		} else {
			log.info("defaultFakeUser=false");
		}

		return http
				// We restrict the scope of this UI securityFilterChain to UI routes
				.securityMatcher(ServerWebExchangeMatchers.pathMatchers(
						// These 2 login routes are authenticated through browser session, build from OAuth2 provider
						"/api/login/v1/user",
						"/api/login/v1/token",
						"/oauth2/**",
						"/",
						"/index.html",
						// Holds static resources (e.g. `/ui/js/store.js`)
						"/ui/js/**",
						// The routes used by the spa
						"/login",
						"/html/**",

						"/swagger-ui.html",
						"/swagger-ui/**",
						"/webjars/**"))
				.authorizeExchange(auth -> auth

						// Login does not requires being loggged-in yet
						.pathMatchers(
								// "/login",
								"/oauth2/**",
								"/",
								"/index.html")
						.permitAll()
						// Swagger UI
						.pathMatchers("/swagger-ui.html", "/swagger-ui/**")
						.permitAll()
						// The route used by the SPA
						.pathMatchers("/login", "/html/**")
						.permitAll()
						// Webjars and static resources
						.pathMatchers("/ui/js/**", "/webjars/**")
						.permitAll()

						// If `fakeUser`, we give free-access to all resources. Else this rule is a no-op, and these
						// routes needs authentication
						.pathMatchers(defaultFakeUser ? "/api/login/v1/user" : "/none")
						.permitAll()
						.pathMatchers(defaultFakeUser ? "/api/login/v1/token" : "/none")
						.permitAll()

						// The rest needs to be authenticated
						.anyExchange()
						.authenticated())

				.formLogin(login -> login.loginPage("http%s://localhost:8080/login".formatted(isSsl ? "s" : ""))
						// Required not to get an NPE at `.build()`
						.authenticationManager(ram))
				.oauth2Login(
						oauth2 -> oauth2.authenticationSuccessHandler(new RedirectServerAuthenticationSuccessHandler(
								"http%s://localhost:8080/api/private/user".formatted(isSsl ? "s" : ""))))

				.build();
	}

	@Order(Ordered.LOWEST_PRECEDENCE)
	@Bean
	public SecurityWebFilterChain configureApi(ServerHttpSecurity http, Environment env) {
		boolean defaultFakePlayer = env.acceptsProfiles(Profiles.of(IKumiteSpringProfiles.P_DEFAULT_FAKE_PLAYER));
		if (defaultFakePlayer) {
			log.warn("defaultFakePlayer=true");
		} else {
			log.info("defaultFakePlayer=false");
		}

		if (DISABLE_CSRF_CORS) {
			// i.e. authentication based on a JWT as header, not automated auth through cookie and session
			log.warn("We disabled CORS and CSRF in API, as the API has stateless auth");
		}

		// The browser will get an JWT given `/api/login/v1/token`. This route is protected by oauth2Login, and will
		// generate a JWT.
		// Given JWT is the only way to authenticate to the rest of the API. `oauth2ResourceServer` shall turns given
		// JWT into a proper Authentication. This is a 2-step process: JWT -> JWTClaimsSet (which will be used to make a
		// Jwt). And later a Jwt to a AbstractAuthenticationToken.
		Converter<JWT, Mono<JWTClaimsSet>> nimbusJwtToClaims = makeSimpleJwtToClaimsConverter();
		// https://docs.spring.io/spring-security/reference/reactive/oauth2/resource-server/jwt.html
		ReactiveJwtDecoder jwtDecoder = new NimbusReactiveJwtDecoder(nimbusJwtToClaims);

		// Default to ReactiveJwtAuthenticationConverter
		// Converter<Jwt, ? extends Mono<? extends AbstractAuthenticationToken>> aa = jwt -> {
		// return null;
		// };

		// DefaultJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
		// // jwtProcessor.setJWSKeySelector(jwsKeySelector);
		// // Spring Security validates the claim set independent from Nimbus
		// jwtProcessor.setJWTClaimsSetVerifier((claims, context) -> {
		// });

		// We can disable CSRF as these routes are stateless, does not rely on any cookie/session, but on some JWT
		return http

				// Store CSRF token in a cookie
				// .csrf(csrf -> csrf.csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse()))
				.csrf(csrf -> {
					if (DISABLE_CSRF_CORS) {
						csrf.disable();
					}
				})
				.cors(cors -> {
					if (DISABLE_CSRF_CORS) {
						cors.disable();
					}
				})

				.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtDecoder(jwtDecoder)
				// Default to ReactiveJwtAuthenticationConverter
				// .jwtAuthenticationConverter(aa)
				))

				.authorizeExchange(auth -> auth
						// Actuator is partly public
						.pathMatchers("/actuator/health/readiness", "/actuator/health/liveness")
						.permitAll()
						// Swagger/OpenAPI is public
						.pathMatchers("/v3/api-docs/**")
						.permitAll()
						// public API is public
						.pathMatchers("/api/public/**")
						.permitAll()
						// Some Login APIs are public
						.pathMatchers("/api/login/v1/providers")
						.permitAll()

						// If `fakePlayer`, we give free-access to all resources. Else this rule is a no-op
						.pathMatchers(defaultFakePlayer ? "/**" : "/none")
						.permitAll()

						// The rest needs to be authenticated
						.anyExchange()
						.authenticated())
				// .oauth2Login(Customizer.withDefaults())
				// .logout((logout) -> logout.logoutSuccessHandler(oidcLogoutSuccessHandler()))

				// Need to diable CORS and CSRF for APIs?
				// .cors(c -> c.disable())
				// .csrf(c -> c.disable())

				// Default OAuth2 behavior is to redirect to login pages
				// If not loged-in, we want to receive 401 and not 302 (which are good for UX)
				.exceptionHandling(e -> {
					BearerTokenServerAuthenticationEntryPoint authenticationEntryPoint =
							new BearerTokenServerAuthenticationEntryPoint();
					authenticationEntryPoint.setRealmName("Kumite Realm");
					e.authenticationEntryPoint(authenticationEntryPoint);
				})

				.anonymous(a -> a.principal("Ano"))
				.build();
	}

	public static Converter<JWT, Mono<JWTClaimsSet>> makeSimpleJwtToClaimsConverter() {
		return jwt -> {
			try {
				return Mono.just(jwt.getJWTClaimsSet());
			} catch (ParseException e) {
				throw new IllegalArgumentException(e);
			}
		};
	}

}
