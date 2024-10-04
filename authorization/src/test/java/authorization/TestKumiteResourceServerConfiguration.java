package authorization;

import java.text.ParseException;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import com.nimbusds.jose.jwk.OctetSequenceKey;

import eu.solven.kumite.oauth2.IKumiteOAuth2Constants;
import eu.solven.kumite.oauth2.resourceserver.KumiteResourceServerConfiguration;
import eu.solven.kumite.tools.JdkUuidGenerator;

public class TestKumiteResourceServerConfiguration {
	KumiteResourceServerConfiguration conf = new KumiteResourceServerConfiguration();

	@Test
	public void testGenerateMultipleTimes() throws ParseException {
		MockEnvironment env = new MockEnvironment();
		env.setProperty(IKumiteOAuth2Constants.KEY_JWT_SIGNINGKEY, IKumiteOAuth2Constants.GENERATE);

		OctetSequenceKey key1 = KumiteResourceServerConfiguration.loadOAuth2SigningKey(env, JdkUuidGenerator.INSTANCE);
		OctetSequenceKey key2 = KumiteResourceServerConfiguration.loadOAuth2SigningKey(env, JdkUuidGenerator.INSTANCE);

		Assertions.assertThat(key1.toJSONString()).isEqualTo(key2.toJSONString());
	}
}
