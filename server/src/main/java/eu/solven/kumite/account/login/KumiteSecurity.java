package eu.solven.kumite.account.login;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.WebExceptionHandler;
import org.springframework.web.server.WebFilter;

import eu.solven.kumite.app.controllers.KumiteLoginController;
import eu.solven.kumite.app.controllers.KumitePublicController;
import eu.solven.kumite.app.controllers.MetadataController;
import eu.solven.kumite.app.webflux.KumiteExceptionRoutingWebFilter;
import eu.solven.kumite.app.webflux.KumiteWebExceptionHandler;

// https://docs.spring.io/spring-security/reference/reactive/oauth2/login/advanced.html#webflux-oauth2-login-advanced-userinfo-endpoint
@RestController
@Import({

		SocialWebFluxSecurity.class,

		KumitePublicController.class,
		KumiteLoginController.class,
		MetadataController.class,

		KumiteJwtSigningConfiguration.class,

})
public class KumiteSecurity {

	@Bean
	WebFilter kumiteExceptionRoutingWebFilter() {
		return new KumiteExceptionRoutingWebFilter();
	}

	@Bean
	WebExceptionHandler kumiteWebExceptionHandler() {
		return new KumiteWebExceptionHandler();
	}
}