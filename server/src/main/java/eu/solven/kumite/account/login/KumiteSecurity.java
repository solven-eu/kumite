package eu.solven.kumite.account.login;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.WebExceptionHandler;
import org.springframework.web.server.WebFilter;

import eu.solven.kumite.app.IKumiteSpringProfiles;
import eu.solven.kumite.app.controllers.KumiteLoginController;
import eu.solven.kumite.app.controllers.KumitePublicController;
import eu.solven.kumite.app.controllers.MetadataController;
import eu.solven.kumite.app.webflux.KumiteExceptionRoutingWebFilter;
import eu.solven.kumite.app.webflux.KumiteWebExceptionHandler;
import lombok.extern.slf4j.Slf4j;

// https://docs.spring.io/spring-security/reference/reactive/oauth2/login/advanced.html#webflux-oauth2-login-advanced-userinfo-endpoint
@RestController
@Import({

		SocialWebFluxSecurity.class,

		KumitePublicController.class,
		KumiteLoginController.class,
		MetadataController.class,

		KumiteJwtSigningConfiguration.class,

})
@Slf4j
public class KumiteSecurity {

	@Profile(IKumiteSpringProfiles.P_PRODMODE)
	@Bean
	public Void checkSecured(Environment env) {
		boolean acceptUnsafe = env.acceptsProfiles(Profiles.of(IKumiteSpringProfiles.P_UNSAFE,
				IKumiteSpringProfiles.P_UNSAFE_SERVER,
				IKumiteSpringProfiles.P_FAKE_USER,
				IKumiteSpringProfiles.P_UNSAFE_EXTERNAL_OAUTH2));

		if (acceptUnsafe) {
			throw new IllegalStateException("At least one unsafe profile is activated");
		}

		if ("NEEDS_TO_BE_DEFINED".equals(env.getProperty("kumite.login.oauth2.github.clientId"))) {
			log.warn("We lack a proper environment variable for XXX");
		} else if ("NEEDS_TO_BE_DEFINED".equals(env.getProperty("kumite.login.oauth2.github.clientSecret"))) {
			log.warn("We lack a proper environment variable for XXX");
		}

		return null;
	}

	@Bean
	public Void checkSpringProfilesConsistency(Environment env) {
		boolean acceptInMemory = env.acceptsProfiles(Profiles.of(IKumiteSpringProfiles.P_INMEMORY));
		boolean acceptRedis = env.acceptsProfiles(Profiles.of(IKumiteSpringProfiles.P_REDIS));

		if (acceptRedis && acceptInMemory) {
			throw new IllegalStateException(
					"Can not be both " + IKumiteSpringProfiles.P_INMEMORY + " and " + IKumiteSpringProfiles.P_REDIS);
		}

		return null;
	}

	@Bean
	WebFilter kumiteExceptionRoutingWebFilter() {
		return new KumiteExceptionRoutingWebFilter();
	}

	@Bean
	WebExceptionHandler kumiteWebExceptionHandler() {
		return new KumiteWebExceptionHandler();
	}
}