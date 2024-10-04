package eu.solven.kumite.oauth2.resourceserver;

import java.text.ParseException;
import java.util.concurrent.atomic.AtomicReference;

import javax.crypto.SecretKey;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

import com.nimbusds.jose.jwk.OctetSequenceKey;

import eu.solven.kumite.app.IKumiteSpringProfiles;
import eu.solven.kumite.oauth2.IKumiteOAuth2Constants;
import eu.solven.kumite.oauth2.authorizationserver.KumiteTokenService;
import eu.solven.kumite.tools.IUuidGenerator;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class KumiteResourceServerConfiguration {

	public static final MacAlgorithm MAC_ALGORITHM = MacAlgorithm.HS256;

	private static final AtomicReference<String> GENERATED_SIGNINGKEY = new AtomicReference<>();

	// https://stackoverflow.com/questions/64758305/how-to-configure-a-reactive-resource-server-to-use-a-jwt-with-a-symmetric-key
	// https://docs.spring.io/spring-security/reference/reactive/oauth2/resource-server/jwt.html
	// The browser will get an JWT given `/api/login/v1/token`. This route is protected by oauth2Login, and will
	// generate a JWT.
	// Given JWT is the only way to authenticate to the rest of the API. `oauth2ResourceServer` shall turns given
	// JWT into a proper Authentication. This is a 2-step process: JWT -> JWTClaimsSet (which will be used to make a
	// Jwt). And later a Jwt to a AbstractAuthenticationToken.
	@Bean
	@SneakyThrows(ParseException.class)
	public ReactiveJwtDecoder jwtDecoder(Environment env, IUuidGenerator uuidGenerator) {
		OctetSequenceKey octetSequenceKey = loadOAuth2SigningKey(env, uuidGenerator);
		SecretKey secretKey = octetSequenceKey.toSecretKey();

		return NimbusReactiveJwtDecoder.withSecretKey(secretKey).macAlgorithm(MAC_ALGORITHM).build();
	}

	public static OctetSequenceKey loadOAuth2SigningKey(Environment env, IUuidGenerator uuidGenerator)
			throws ParseException {
		String secretKeySpec = env.getRequiredProperty(IKumiteOAuth2Constants.KEY_JWT_SIGNINGKEY);

		if ("NEEDS_TO_BE_DEFINED".equals(secretKeySpec)) {
			throw new IllegalStateException("Lack proper `" + IKumiteOAuth2Constants.KEY_JWT_SIGNINGKEY
					+ "` or spring.profiles.active="
					+ IKumiteSpringProfiles.P_UNSAFE_SERVER);
		} else if (IKumiteOAuth2Constants.GENERATE.equals(secretKeySpec)) {
			if (env.acceptsProfiles(Profiles.of(IKumiteSpringProfiles.P_PRDMODE))) {
				throw new IllegalStateException("Can not GENERATE oauth2 signingKey in `prodmode`");
			}
			// Ensure we generate a signingKey only once, so that the key in IJwtDecoder and the token in Bearer token
			// are based on the same signingKey
			synchronized (secretKeySpec) {
				if (GENERATED_SIGNINGKEY.get() == null) {
					log.warn("We generate a random signingKey");
					secretKeySpec = KumiteTokenService.generateSignatureSecret(uuidGenerator).toJSONString();
					GENERATED_SIGNINGKEY.set(secretKeySpec);
				} else {
					secretKeySpec = GENERATED_SIGNINGKEY.get();
				}
			}
		}

		OctetSequenceKey octetSequenceKey = OctetSequenceKey.parse(secretKeySpec);
		return octetSequenceKey;
	}
}
