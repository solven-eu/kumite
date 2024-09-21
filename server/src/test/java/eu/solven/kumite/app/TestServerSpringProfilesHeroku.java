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

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = { EmptySpringBootApplication.class }, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles(IKumiteSpringProfiles.P_HEROKU)
@Slf4j
public class TestServerSpringProfilesHeroku implements IKumiteSpringProfiles {

	@Autowired
	Environment env;

	@Test
	public void testSpringProfiles() {

		Assertions.assertThat(env.acceptsProfiles(Profiles.of(P_DEFAULT_SERVER))).isTrue();
		Assertions.assertThat(env.acceptsProfiles(Profiles.of(P_INJECT_DEFAULT_GAMES))).isTrue();
		// Heroku alone does not trigger Redis
		Assertions.assertThat(env.acceptsProfiles(Profiles.of(P_REDIS))).isFalse();
		Assertions.assertThat(env.acceptsProfiles(Profiles.of(P_INMEMORY))).isFalse();

		// By default, we include unsafe parameters
		Assertions.assertThat(env.acceptsProfiles(Profiles.of(P_UNSAFE_SERVER))).isFalse();
		Assertions.assertThat(env.acceptsProfiles(Profiles.of(P_FAKEUSER))).isFalse();
		Assertions.assertThat(env.acceptsProfiles(Profiles.of(P_UNSAFE_EXTERNAL_OAUTH2))).isFalse();
	}
}