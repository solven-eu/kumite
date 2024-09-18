package eu.solven.kumite.account.login;

import java.text.ParseException;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtReactiveAuthenticationManager;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.OctetSequenceKey;
import com.nimbusds.jwt.SignedJWT;

import eu.solven.kumite.account.KumiteUser;
import eu.solven.kumite.scenario.TestTSPLifecycle;
import eu.solven.kumite.tools.JdkUuidGenerator;

public class TestKumiteTokenService {
	MockEnvironment env = new MockEnvironment();
	KumiteTokenService tokenService = new KumiteTokenService(env, JdkUuidGenerator.INSTANCE);

	@Test
	public void testJwt_randomSecret() throws JOSEException, ParseException {
		JWK signatureSecret = tokenService.generateSignatureSecret();
		env.setProperty(KumiteTokenService.KEY_JWT_SIGNINGKEY, signatureSecret.toJSONString());

		UUID accountId = UUID.randomUUID();
		UUID playerId = UUID.randomUUID();
		KumiteUser user =
				KumiteUser.builder().accountId(accountId).playerId(playerId).raw(TestTSPLifecycle.userRaw()).build();
		String accessToken = tokenService.generateAccessToken(user, Set.of(playerId), Duration.ofMinutes(1));

		{
			JWSVerifier verifier = new MACVerifier((OctetSequenceKey) signatureSecret);

			SignedJWT signedJWT = SignedJWT.parse(accessToken);
			Assertions.assertThat(signedJWT.verify(verifier)).isTrue();
		}

		JwtReactiveAuthenticationManager authManager =
				new JwtReactiveAuthenticationManager(new KumiteJwtSigningConfiguration().jwtDecoder(env, tokenService));

		Authentication auth = authManager.authenticate(new BearerTokenAuthenticationToken(accessToken)).block();

		Assertions.assertThat(auth.getPrincipal()).isOfAnyClassIn(Jwt.class);
		Jwt jwt = (Jwt) auth.getPrincipal();
		Assertions.assertThat(jwt.getSubject()).isEqualTo(accountId.toString());
		Assertions.assertThat(jwt.getAudience()).containsExactly("Kumite-Server");
		Assertions.assertThat(jwt.getClaimAsStringList("playerIds")).isEqualTo(List.of(playerId.toString()));
	}
}
