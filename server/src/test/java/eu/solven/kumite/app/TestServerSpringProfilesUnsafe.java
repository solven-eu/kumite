package eu.solven.kumite.app;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import lombok.extern.slf4j.Slf4j;

/**
 * This tests the default configuration: we rely on multiple mechanisms circumventing security.
 * 
 * @author Benoit Lacelle
 *
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = { EmptySpringBootApplication.class }, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles({ IKumiteSpringProfiles.P_UNSAFE, IKumiteSpringProfiles.P_INMEMORY })
@Slf4j
public class TestServerSpringProfilesUnsafe implements IKumiteSpringProfiles {

	@Autowired
	Environment env;

	@Test
	public void testSpringProfiles() {
		Assertions.assertThat(env.acceptsProfiles(Profiles.of(P_DEFAULT_SERVER))).isTrue();
		Assertions.assertThat(env.acceptsProfiles(Profiles.of(P_INJECT_DEFAULT_GAMES))).isTrue();

		Assertions.assertThat(env.acceptsProfiles(Profiles.of(P_REDIS))).isFalse();
		Assertions.assertThat(env.acceptsProfiles(Profiles.of(P_INMEMORY))).isTrue();

		// By default, we include `unsafe` parameters
		Assertions.assertThat(env.acceptsProfiles(Profiles.of(P_UNSAFE_SERVER))).isTrue();
		Assertions.assertThat(env.acceptsProfiles(Profiles.of(P_UNSAFE_EXTERNAL_OAUTH2))).isTrue();
		// But `unsafe` should not include `fakeuser`. Either one relies on `default`, or should ask explicitly
		// `unsafe+fakeuser`.
		Assertions.assertThat(env.acceptsProfiles(Profiles.of(P_FAKEUSER))).isFalse();
	}
}