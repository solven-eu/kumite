package eu.solven.kumite.account.login;

import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.web.server.BearerTokenServerAuthenticationEntryPoint;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;

import lombok.RequiredArgsConstructor;

@EnableWebFluxSecurity
@Import({

		KumiteOAuth2UserService.class,

})
@RequiredArgsConstructor
public class SocialWebFluxSecurity {

	// https://github.com/ch4mpy/spring-addons/tree/master/samples/tutorials/resource-server_with_ui
	// https://stackoverflow.com/questions/74744901/default-401-instead-of-redirecting-for-oauth2-login-spring-security

	@Order(Ordered.LOWEST_PRECEDENCE - 1)
	@Bean
	public SecurityWebFilterChain configureUi(ServerProperties serverProperties, ServerHttpSecurity http) {
		boolean isSsl = serverProperties.getSsl() != null && serverProperties.getSsl().isEnabled();

		ReactiveAuthenticationManager ram = auth -> {
			throw new IllegalStateException();
		};

		return http
				// We restrict the scope of this UI securityFilterChain to UI routes
				.securityMatcher(ServerWebExchangeMatchers.pathMatchers("/login/**",
						"/oauth2/**",
						"/",
						"/index.html",
						"/ui/**",
						"/swagger-ui.html",
						"/swagger-ui/**",
						"/webjars/**"))
				.authorizeExchange(auth -> auth

						// Login does not requires being loggged-int yet
						.pathMatchers("/login/**", "/oauth2/**", "/", "/index.html", "/ui/**")
						.permitAll()
						// Swagger UI
						.pathMatchers("/swagger-ui.html", "/swagger-ui/**")
						.permitAll()
						// Webjars
						.pathMatchers("/webjars/**")
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
	public SecurityWebFilterChain configureApi(ServerHttpSecurity http) {
		return http.authorizeExchange(auth -> auth

				// Actuator is partly public
				.pathMatchers("/actuator/health/readiness", "/actuator/health/liveness")
				.permitAll()
				// Swagger/OpenAPI is public
				.pathMatchers("/v3/api-docs/**")
				.permitAll()
				// public API is public
				.pathMatchers("/api/public")
				.permitAll()

				// The rest needs to be authenticated
				.anyExchange()
				.authenticated())
				.oauth2Login(Customizer.withDefaults())
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

				.build();
	}

}
