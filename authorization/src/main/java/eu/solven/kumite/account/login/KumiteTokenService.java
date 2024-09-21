package eu.solven.kumite.account.login;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;

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
import eu.solven.kumite.login.AccessTokenWrapper;
import eu.solven.kumite.login.RefreshTokenWrapper;
import eu.solven.kumite.tools.IUuidGenerator;
import eu.solven.kumite.tools.JdkUuidGenerator;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KumiteTokenService {
	// https://connect2id.com/products/server/docs/api/token#url
	public static final String ENV_OAUTH2_ISSUER = "kumite.oauth2.issuer-base-url";

	public static final String KEY_JWT_SIGNINGKEY = "kumite.oauth2.signing-key";
	// Expect a value parsable by `Duration.parse`
	// public static final String KEY_ACCESSTOKEN_EXP = "kumite.login.oauth2_exp";
	// Expect a value parsable by `Duration.parse`
	// public static final String KEY_REFRESHTOKEN_EXP = "kumite.login.oauth2_exp";

	final Environment env;
	final IUuidGenerator uuidGenerator;
	// BEWARE We would prefer a RSA KeyPair for PRD
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

	public static void main(String[] args) {
		Environment env = new StandardEnvironment();
		JWK secretKey = new KumiteTokenService(env, new JdkUuidGenerator()).generateSignatureSecret();
		System.out.println("Secret key for JWT signing: " + secretKey.toJSONString());
	}

	@SneakyThrows(JOSEException.class)
	JWK generateSignatureSecret() {
		// https://connect2id.com/products/nimbus-jose-jwt/examples/jws-with-hmac
		// Generate random 256-bit (32-byte) shared secret
		// SecureRandom random = new SecureRandom();
		//
		String rawNbBits = KumiteJwtSigningConfiguration.MAC_ALGORITHM.getName().substring("HS".length());
		int nbBits = Integer.parseInt(rawNbBits);

		OctetSequenceKey jwk = new OctetSequenceKeyGenerator(nbBits).keyID(uuidGenerator.randomUUID().toString())
				.algorithm(JWSAlgorithm.parse(KumiteJwtSigningConfiguration.MAC_ALGORITHM.getName()))
				.issueTime(new Date())
				.generate();

		return jwk;
	}

	public String generateAccessToken(KumiteUser user,
			Set<UUID> playerIds,
			Duration accessTokenValidity,
			boolean isRefreshToken) {
		// Generating a Signed JWT
		// https://auth0.com/blog/rs256-vs-hs256-whats-the-difference/
		// https://security.stackexchange.com/questions/194830/recommended-asymmetric-algorithms-for-jwt
		// https://curity.io/resources/learn/jwt-best-practices/
		JWSHeader.Builder headerBuilder =
				new JWSHeader.Builder(JWSAlgorithm.parse(KumiteJwtSigningConfiguration.MAC_ALGORITHM.getName()))
						.type(JOSEObjectType.JWT);

		Instant now = Instant.now();

		String issuer = getIssuer();
		JWTClaimsSet.Builder claimsSetBuilder = new JWTClaimsSet.Builder().subject(user.getAccountId().toString())
				.audience("Kumite-Server")
				// https://connect2id.com/products/server/docs/api/token#url
				.issuer(issuer)
				// https://www.oauth.com/oauth2-servers/access-tokens/self-encoded-access-tokens/
				// This JWTId is very important, as it could be used to ban some access_token used by some lost VM
				// running some bot.
				.jwtID(uuidGenerator.randomUUID().toString())
				.issueTime(Date.from(now))
				.notBeforeTime(Date.from(now))
				.expirationTime(Date.from(now.plus(accessTokenValidity)))
				.claim("refresh_token", isRefreshToken)
				.claim("playerIds", playerIds);

		SignedJWT signedJWT = new SignedJWT(headerBuilder.build(), claimsSetBuilder.build());

		try {
			JWSSigner signer = new MACSigner(supplierSymetricKey.get());
			signedJWT.sign(signer);
		} catch (JOSEException e) {
			throw new IllegalStateException("Issue signing the JWT", e);
		}

		return signedJWT.serialize();
	}

	private String getIssuer() {
		String issuerBaseUrl = env.getRequiredProperty(ENV_OAUTH2_ISSUER);
		if ("NEEDS_TO_BE_DEFINED".equals(issuerBaseUrl)) {
			throw new IllegalStateException("Need to setup %s".formatted(ENV_OAUTH2_ISSUER));
		}
		// This matches `/api/v1/oauth2/token` as route for token generation
		return issuerBaseUrl + "/api/v1/oauth2";
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
	public AccessTokenWrapper wrapInJwtAccessToken(KumiteUser user, UUID playerId) {
		Duration accessTokenValidity = Duration.parse("PT1H");

		String accessToken = generateAccessToken(user, Set.of(playerId), accessTokenValidity, false);

		// https://www.oauth.com/oauth2-servers/access-tokens/access-token-response/
		return AccessTokenWrapper.builder()
				.accessToken(accessToken)
				.playerId(playerId)
				.tokenType("Bearer")
				.expiresIn(accessTokenValidity.toSeconds())
				.build();

	}

	// https://stackoverflow.com/questions/38986005/what-is-the-purpose-of-a-refresh-token
	// https://stackoverflow.com/questions/40555855/does-the-refresh-token-expire-and-if-so-when
	public RefreshTokenWrapper wrapInJwtRefreshToken(KumiteUser user, Set<UUID> playerIds) {
		Duration refreshTokenValidity = Duration.parse("P365D");

		String accessToken = generateAccessToken(user, playerIds, refreshTokenValidity, true);

		// https://www.oauth.com/oauth2-servers/access-tokens/access-token-response/
		return RefreshTokenWrapper.builder()
				.refreshToken(accessToken)
				.playerIds(playerIds)
				.tokenType("Bearer")
				.expiresIn(refreshTokenValidity.toSeconds())
				.build();

	}

}
