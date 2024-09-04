package eu.solven.kumite.account.login;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.springframework.core.env.Environment;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import eu.solven.kumite.account.KumiteUser;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;

@RequiredArgsConstructor
public class KumiteTokenService {
	public static final String KEY_JWT_SIGNINGKEY = "kumite.login.signing-key";

	final Environment env;

	public Map<String, ?> wrapInJwtToken(KumiteUser user) {
		String accessToken = generateAccessToken(user);
		return Map.of("access_token", accessToken);
	}

	public static void main(String[] args) {
		ECKey secretKey = generateSecret();
		System.out.println("Secret key for JWT signing: " + secretKey.toJSONString());
	}

	@SneakyThrows(JOSEException.class)
	static ECKey generateSecret() {
		ECKey ecKey =
				new ECKeyGenerator(Curve.P_256).keyUse(KeyUse.SIGNATURE).keyID(UUID.randomUUID().toString()).generate();

		return ecKey;
	}

	/**
	 * Generates an access token corresponding to provided user entity based on configured settings. The generated
	 * access token can be used to perform tasks on behalf of the user on subsequent HTTP calls to the application until
	 * it expires or is revoked.
	 * 
	 * @param user
	 *            The user for whom to generate an access token.
	 * @throws IllegalArgumentException
	 *             if provided argument is <code>null</code>.
	 * @return The generated JWT access token.
	 * @throws IllegalStateException
	 */
	@SneakyThrows({ JOSEException.class, IllegalStateException.class, ParseException.class })
	public String generateAccessToken(KumiteUser user) {
		Duration accessTokenValidity = Duration.ofHours(1);
		long expirationMs = accessTokenValidity.toMillis();

		// Generating a Secret
		val ecKey = ECKey.parse(env.getRequiredProperty(KEY_JWT_SIGNINGKEY));

		// Generating a Signed JWT
		val headerBuilder =
				new JWSHeader.Builder(JWSAlgorithm.ES256).type(new JOSEObjectType("JWT")).jwk(ecKey.toPublicJWK());

		Date curDate = new Date();
		JWTClaimsSet.Builder claimsSetBuilder = new JWTClaimsSet.Builder().subject(user.getAccountId().toString())
				.audience("Kumite-Server")
				.issuer("https://kumite.com")
				.jwtID(UUID.randomUUID().toString())
				.issueTime(curDate)
				.notBeforeTime(Date.from(Instant.now()))
				.expirationTime(Date.from(Instant.now().plusMillis(expirationMs)))
				.claim("mainPlayerId", user.getPlayerId().toString());

		SignedJWT signedJWT = new SignedJWT(headerBuilder.build(), claimsSetBuilder.build());
		signedJWT.sign(new ECDSASigner(ecKey));

		return signedJWT.serialize();
	}
}
