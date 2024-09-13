package eu.solven.kumite.account.login;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import org.springframework.core.env.Environment;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jose.jwk.gen.OctetSequenceKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import eu.solven.kumite.account.KumiteUser;
import eu.solven.kumite.tools.IUuidGenerator;
import eu.solven.kumite.tools.JdkUuidGenerator;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KumiteTokenService {
	public static final String KEY_JWT_SIGNINGKEY = "kumite.login.signing-key";
	// Expect a value parsable by `Duration.parse`
	public static final String KEY_ACCESSTOKEN_EXP = "kumite.login.oauth2_exp";

	final Environment env;
	final IUuidGenerator uuidGenerator;
	final Supplier<OctetSequenceKey> supplierSymetricKey;

	public KumiteTokenService(Environment env, IUuidGenerator uuidgenerator) {
		this.env = env;
		this.uuidGenerator = uuidgenerator;
		this.supplierSymetricKey = () -> loadSigningJwk();
	}

	@SneakyThrows({ IllegalStateException.class, ParseException.class })
	private OctetSequenceKey loadSigningJwk() {
		return OctetSequenceKey.parse(env.getRequiredProperty(KEY_JWT_SIGNINGKEY));
	}

	public Map<String, ?> wrapInJwtToken(KumiteUser user, UUID playerId) {
		String accessToken = generateAccessToken(user, playerId);
		return Map.of("access_token", accessToken, "player_id", playerId);
	}

	public static void main(String[] args) {
		JWK secretKey = generateSignatureSecret(new JdkUuidGenerator());
		System.out.println("Secret key for JWT signing: " + secretKey.toJSONString());
	}

	@SneakyThrows(JOSEException.class)
	static JWK generateSignatureSecret(IUuidGenerator uuidgenerator) {
		// https://connect2id.com/products/nimbus-jose-jwt/examples/jws-with-hmac
		// Generate random 256-bit (32-byte) shared secret
		// SecureRandom random = new SecureRandom();
		//
		String rawNbBits = KumiteJwtSigningConfiguration.MAC_ALGORITHM.getName().substring("HS".length());
		int nbBits = Integer.parseInt(rawNbBits);

		OctetSequenceKey jwk = new OctetSequenceKeyGenerator(nbBits).keyID(uuidgenerator.randomUUID().toString())
				.algorithm(JWSAlgorithm.parse(KumiteJwtSigningConfiguration.MAC_ALGORITHM.getName()))
				.issueTime(new Date())
				.generate();

		return jwk;
	}

	/**
	 * Generates an access token corresponding to provided user entity based on configured settings. The generated
	 * access token can be used to perform tasks on behalf of the user on subsequent HTTP calls to the application until
	 * it expires or is revoked.
	 * 
	 * @param user
	 *            The user for whom to generate an access token.
	 * @param playerId
	 * @throws IllegalArgumentException
	 *             if provided argument is <code>null</code>.
	 * @return The generated JWT access token.
	 * @throws IllegalStateException
	 */
	@SneakyThrows({ JOSEException.class })
	public String generateAccessToken(KumiteUser user, UUID playerId) {
		Duration accessTokenValidity = Duration.parse(env.getProperty(KEY_ACCESSTOKEN_EXP, "PT1H"));

		if (accessTokenValidity.compareTo(Duration.parse("PT1H")) > 0) {
			log.warn("Unusual expiry for accessToken: {}", accessTokenValidity);
		}

		long expirationMs = accessTokenValidity.toMillis();

		// Generating a Signed JWT
		// https://auth0.com/blog/rs256-vs-hs256-whats-the-difference/
		// https://security.stackexchange.com/questions/194830/recommended-asymmetric-algorithms-for-jwt
		// https://curity.io/resources/learn/jwt-best-practices/
		JWSHeader.Builder headerBuilder =
				new JWSHeader.Builder(JWSAlgorithm.parse(KumiteJwtSigningConfiguration.MAC_ALGORITHM.getName()))
						.type(JOSEObjectType.JWT);

		Date curDate = new Date();
		JWTClaimsSet.Builder claimsSetBuilder = new JWTClaimsSet.Builder().subject(user.getAccountId().toString())
				.audience("Kumite-Server")
				.issuer("https://kumite.com")
				.jwtID(uuidGenerator.randomUUID().toString())
				.issueTime(curDate)
				.notBeforeTime(Date.from(Instant.now()))
				.expirationTime(Date.from(Instant.now().plusMillis(expirationMs)))
				.claim("playerId", playerId.toString());

		SignedJWT signedJWT = new SignedJWT(headerBuilder.build(), claimsSetBuilder.build());

		JWSSigner signer = new MACSigner(supplierSymetricKey.get());
		signedJWT.sign(signer);

		return signedJWT.serialize();
	}
}
