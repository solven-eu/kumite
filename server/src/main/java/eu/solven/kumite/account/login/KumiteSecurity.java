package eu.solven.kumite.account.login;

import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RestController;

// https://docs.spring.io/spring-security/reference/reactive/oauth2/login/advanced.html#webflux-oauth2-login-advanced-userinfo-endpoint
@RestController
@Import({ SocialWebFluxSecurity.class,

		KumitePublicController.class,
		KumiteLoginController.class,

		KumitePrivateController.class,

})
public class KumiteSecurity {
	// @Bean
	// public ClientRegistrationRepository clientRegistrationRepository(List<ClientRegistration> clientRegistrations) {
	// return new InMemoryClientRegistrationRepository(clientRegistrations.toArray(ClientRegistration[]::new));
	// }
}