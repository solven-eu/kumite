package eu.solven.kumite.account.login;

import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtReactiveAuthenticationManager;

import eu.solven.kumite.account.KumiteUser;
import eu.solven.kumite.scenario.TestTSPLifecycle;

public class TestKumiteTokenService {
	MockEnvironment env = new MockEnvironment();
	KumiteTokenService tokenService = new KumiteTokenService(env);

	@Test
	public void testJwt() {
		env.setProperty(KumiteTokenService.KEY_JWT_SIGNINGKEY, KumiteTokenService.generateSecret().toJSONString());

		UUID accountId = UUID.randomUUID();
		UUID playerId = UUID.randomUUID();
		KumiteUser user =
				KumiteUser.builder().accountId(accountId).playerId(playerId).raw(TestTSPLifecycle.userRaw()).build();
		String accessToken = tokenService.generateAccessToken(user);

		JwtReactiveAuthenticationManager authManager = new JwtReactiveAuthenticationManager(
				new NimbusReactiveJwtDecoder(SocialWebFluxSecurity.makeSimpleJwtToClaimsConverter()));

		Authentication auth = authManager.authenticate(new BearerTokenAuthenticationToken(accessToken)).block();

		Assertions.assertThat(auth.getPrincipal()).isOfAnyClassIn(Jwt.class);
		Jwt jwt = (Jwt) auth.getPrincipal();
		Assertions.assertThat(jwt.getSubject()).isEqualTo(accountId.toString());
		Assertions.assertThat(jwt.getAudience()).containsExactly("Kumite-Server");
		Assertions.assertThat(jwt.getClaimAsString("mainPlayerId")).isEqualTo(playerId.toString());

	}
}
