package eu.solven.kumite.account.login;

import java.text.ParseException;

import javax.crypto.SecretKey;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

import com.nimbusds.jose.jwk.OctetSequenceKey;

import eu.solven.kumite.app.IKumiteSpringProfiles;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Import({

		KumiteTokenService.class,

})
@Slf4j
public class KumiteJwtSigningConfiguration {

	public static final MacAlgorithm MAC_ALGORITHM = MacAlgorithm.HS256;

	// https://stackoverflow.com/questions/64758305/how-to-configure-a-reactive-resource-server-to-use-a-jwt-with-a-symmetric-key
	// https://docs.spring.io/spring-security/reference/reactive/oauth2/resource-server/jwt.html
	// The browser will get an JWT given `/api/login/v1/token`. This route is protected by oauth2Login, and will
	// generate a JWT.
	// Given JWT is the only way to authenticate to the rest of the API. `oauth2ResourceServer` shall turns given
	// JWT into a proper Authentication. This is a 2-step process: JWT -> JWTClaimsSet (which will be used to make a
	// Jwt). And later a Jwt to a AbstractAuthenticationToken.
	@Bean
	@SneakyThrows(ParseException.class)
	public ReactiveJwtDecoder jwtDecoder(Environment env, KumiteTokenService kumiteTokenService) {
		String secretKeySpec = env.getRequiredProperty(KumiteTokenService.KEY_JWT_SIGNINGKEY);

		if ("NEEDS_TO_BE_DEFINED".equals(secretKeySpec)) {
			throw new IllegalStateException("Lack proper `" + KumiteTokenService.KEY_JWT_SIGNINGKEY
					+ "` or spring.profiles.active="
					+ IKumiteSpringProfiles.P_UNSAFE_SERVER);
		} else if ("GENERATE".equals(secretKeySpec)) {
			// if (env.acceptsProfiles(Profiles.of(IKumiteSpringProfiles.P_PRODMODE))) {
			// throw new IllegalStateException("Can not GENERATE oauth2 signingKey in `prodmode`");
			// }
			log.warn("We generate a random signingKey");
			secretKeySpec = kumiteTokenService.generateSignatureSecret().toJSONString();
		}

		OctetSequenceKey octetSequenceKey = OctetSequenceKey.parse(secretKeySpec);
		SecretKey secretKey = octetSequenceKey.toSecretKey();

		return NimbusReactiveJwtDecoder.withSecretKey(secretKey).macAlgorithm(MAC_ALGORITHM).build();
	}
}
