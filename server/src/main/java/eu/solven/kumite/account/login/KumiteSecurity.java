package eu.solven.kumite.account.login;

import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RestController;

import eu.solven.kumite.app.controllers.KumiteLoginController;
import eu.solven.kumite.app.controllers.KumitePublicController;
import eu.solven.kumite.app.controllers.MetadataController;

// https://docs.spring.io/spring-security/reference/reactive/oauth2/login/advanced.html#webflux-oauth2-login-advanced-userinfo-endpoint
@RestController
@Import({ SocialWebFluxSecurity.class,

		KumitePublicController.class,
		KumiteLoginController.class,
		MetadataController.class,

		KumiteTokenService.class,

})
public class KumiteSecurity {
}