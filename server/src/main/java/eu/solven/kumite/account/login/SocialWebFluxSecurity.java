package eu.solven.kumite.account.login;

import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.web.server.BearerTokenServerAuthenticationEntryPoint;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;

import com.nimbusds.jwt.JWT;

import eu.solven.kumite.app.IKumiteSpringProfiles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

		boolean fakeUser = env.acceptsProfiles(Profiles.of(IKumiteSpringProfiles.P_FAKE_USER));
		if (fakeUser) {
			log.warn("{}=true", IKumiteSpringProfiles.P_FAKE_USER);
		} else {
			log.info("{}=false", IKumiteSpringProfiles.P_FAKE_USER);
		}

		return http
				// We restrict the scope of this UI securityFilterChain to UI routes
				.securityMatcher(ServerWebExchangeMatchers.pathMatchers(
						// These 2 login routes are authenticated through browser session, build from OAuth2 provider
						"/api/login/v1/user",
						"/api/login/v1/token",
						"/oauth2/**",
						// Holds static resources (e.g. `/ui/js/store.js`)
						"/ui/js/**",
						"/ui/img/**",
						// The routes used by the spa
						"/",
						"/favicon.ico",
						// "/login",
						"/html/**",

						"/login/oauth2/code/*",

						"/swagger-ui.html",
						"/swagger-ui/**",
						"/webjars/**"))
				.authorizeExchange(auth -> auth

						// Login does not requires being loggged-in yet
						.pathMatchers(
								// "/login",
								"/login/oauth2/code/**")
						.permitAll()
						// Swagger UI
						.pathMatchers("/swagger-ui.html", "/swagger-ui/**")
						.permitAll()
						// The route used by the SPA
						.pathMatchers("/", "/html/**"
						// ,"/login"
						)
						.permitAll()
						// Webjars and static resources
						.pathMatchers("/ui/js/**", "/ui/img/**", "/webjars/**", "/favicon.ico")
						.permitAll()

						// If there is no logged-in user, we return a 401
						.pathMatchers("/api/login/v1/user")
						.permitAll()

						// If `fakeUser`, we give free-access to all resources. Else this rule is a no-op, and these
						// routes needs authentication
						.pathMatchers(fakeUser ? "/api/login/v1/token" : "/none")
						.permitAll()

						// The rest needs to be authenticated
						.anyExchange()
						.authenticated())

				.formLogin(login -> login.loginPage("http%s://localhost:8080/html/login".formatted(isSsl ? "s" : ""))
						// Required not to get an NPE at `.build()`
						.authenticationManager(ram))
				// How to request prompt=consent for Github?
				// https://docs.spring.io/spring-security/reference/servlet/oauth2/client/authorization-grants.html
				// https://stackoverflow.com/questions/74242738/how-to-logout-from-oauth-signed-in-web-app-with-github
				.oauth2Login(
						oauth2 -> oauth2.authenticationSuccessHandler(new RedirectServerAuthenticationSuccessHandler(
								"http%s://localhost:8080/html/login?success".formatted(isSsl ? "s" : ""))))

				.build();
	}

	/**
	 * 
	 * @param http
	 * @param env
	 * @param jwtDecoder
	 *            Knows how to check a {@link JWT}, and convert it into a {@link Jwt}. Typically provided from
	 *            {@link KumiteJwtSigningConfiguration}
	 * @return
	 */
	@Order(Ordered.LOWEST_PRECEDENCE)
	@Bean
	public SecurityWebFilterChain configureApi(ServerHttpSecurity http,
			Environment env,
			ReactiveJwtDecoder jwtDecoder) {
		if (DISABLE_CSRF_CORS) {
			// i.e. authentication based on a JWT as header, not automated auth through cookie and session
			log.info("We disabled CORS and CSRF in API, as the API has stateless auth");
		}

		boolean fakeUser = env.acceptsProfiles(Profiles.of(IKumiteSpringProfiles.P_FAKE_USER));
		if (fakeUser) {
			log.warn("{}=true", IKumiteSpringProfiles.P_FAKE_USER);
		} else {
			log.info("{}=false", IKumiteSpringProfiles.P_FAKE_USER);
		}

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

				.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtDecoder(jwtDecoder)))

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

						// If fakeUser==true, we allow the reset route (for integration tests)
						.pathMatchers(fakeUser ? "/api/clear" : "nonono")
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
