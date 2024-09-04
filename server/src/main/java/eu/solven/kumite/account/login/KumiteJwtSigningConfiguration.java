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

import lombok.SneakyThrows;

@Configuration
@Import({

		KumiteTokenService.class,

})
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
	public ReactiveJwtDecoder jwtDecoder(Environment env) {
		String secretKeySpec = env.getRequiredProperty(KumiteTokenService.KEY_JWT_SIGNINGKEY);

		OctetSequenceKey octetSequenceKey = OctetSequenceKey.parse(secretKeySpec);
		SecretKey secretKey = octetSequenceKey.toSecretKey();

		return NimbusReactiveJwtDecoder.withSecretKey(secretKey).macAlgorithm(MAC_ALGORITHM).build();
	}
}
