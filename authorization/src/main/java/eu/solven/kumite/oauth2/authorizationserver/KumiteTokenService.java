package eu.solven.kumite.oauth2.authorizationserver;

import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Set;
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

import eu.solven.kumite.account.internal.KumiteUserRaw;
import eu.solven.kumite.login.AccessTokenWrapper;
import eu.solven.kumite.login.RefreshTokenWrapper;
import eu.solven.kumite.oauth2.IKumiteOAuth2Constants;
import eu.solven.kumite.oauth2.resourceserver.KumiteResourceServerConfiguration;
import eu.solven.kumite.tools.IUuidGenerator;
import eu.solven.kumite.tools.JdkUuidGenerator;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KumiteTokenService {

	final Environment env;
	final IUuidGenerator uuidGenerator;
	// BEWARE We would prefer a RSA KeyPair for PRD
	final Supplier<OctetSequenceKey> supplierSymetricKey;

	public KumiteTokenService(Environment env, IUuidGenerator uuidgenerator) {
		this.env = env;
		this.uuidGenerator = uuidgenerator;
		this.supplierSymetricKey = () -> loadSigningJwk();

		log.info("iss={}", getIssuer());
		log.info("{}.kid={}", IKumiteOAuth2Constants.KEY_JWT_SIGNINGKEY, loadSigningJwk().getKeyID());
	}

	@SneakyThrows({ IllegalStateException.class, ParseException.class })
	private OctetSequenceKey loadSigningJwk() {
		return KumiteResourceServerConfiguration.loadOAuth2SigningKey(env, uuidGenerator);
	}

	public static void main(String[] args) {
		JWK secretKey = KumiteTokenService.generateSignatureSecret(new JdkUuidGenerator());
		System.out.println("Secret key for JWT signing: " + secretKey.toJSONString());
	}

	@SneakyThrows(JOSEException.class)
	public static JWK generateSignatureSecret(IUuidGenerator uuidGenerator) {
		// https://connect2id.com/products/nimbus-jose-jwt/examples/jws-with-hmac
		// Generate random 256-bit (32-byte) shared secret
		// SecureRandom random = new SecureRandom();
		//
		String rawNbBits = KumiteResourceServerConfiguration.MAC_ALGORITHM.getName().substring("HS".length());
		int nbBits = Integer.parseInt(rawNbBits);

		OctetSequenceKey jwk = new OctetSequenceKeyGenerator(nbBits).keyID(uuidGenerator.randomUUID().toString())
				.algorithm(JWSAlgorithm.parse(KumiteResourceServerConfiguration.MAC_ALGORITHM.getName()))
				.issueTime(new Date())
				.generate();

		return jwk;
	}

	public String generateAccessToken(KumiteUserRaw user,
			Set<UUID> playerIds,
			Duration accessTokenValidity,
			boolean isRefreshToken) {
		if (!isRefreshToken && playerIds.size() != 1) {
			throw new IllegalArgumentException("access_token are generated for a specific single playerId");
		}

		// Generating a Signed JWT
		// https://auth0.com/blog/rs256-vs-hs256-whats-the-difference/
		// https://security.stackexchange.com/questions/194830/recommended-asymmetric-algorithms-for-jwt
		// https://curity.io/resources/learn/jwt-best-practices/
		JWSHeader.Builder headerBuilder =
				new JWSHeader.Builder(JWSAlgorithm.parse(KumiteResourceServerConfiguration.MAC_ALGORITHM.getName()))
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
		String issuerBaseUrl = env.getRequiredProperty(IKumiteOAuth2Constants.KEY_OAUTH2_ISSUER);
		if ("NEEDS_TO_BE_DEFINED".equals(issuerBaseUrl)) {
			throw new IllegalStateException("Need to setup %s".formatted(IKumiteOAuth2Constants.KEY_OAUTH2_ISSUER));
		}
		// This matches `/api/v1/oauth2/token` as route for token generation
		return issuerBaseUrl + "/api/v1" + "/oauth2";
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
	public AccessTokenWrapper wrapInJwtAccessToken(KumiteUserRaw user, UUID playerId) {
		// access_token are short-lived
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
	public RefreshTokenWrapper wrapInJwtRefreshToken(KumiteUserRaw user, Set<UUID> playerIds) {
		// refresh_token are long-lived
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
