package eu.solven.kumite.redis;

import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.solven.kumite.account.KumiteUserRawRaw;
import eu.solven.kumite.app.IKumiteSpringProfiles;
import eu.solven.kumite.app.KumiteContestServerApplication;
import eu.solven.kumite.oauth2.IKumiteOAuth2Constants;
import lombok.extern.slf4j.Slf4j;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = { KumiteContestServerApplication.class }, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles({ IKumiteSpringProfiles.P_HEROKU, IKumiteSpringProfiles.P_REDIS })
@TestPropertySource(properties = {
		// https://github.com/spring-projects/spring-boot/issues/4877
		"rediscloud.url" + "=redis://localhost:6379",
		"spring.data.redis.host" + "=localhost",
		"spring.data.redis.port" + "=6379",
		IKumiteOAuth2Constants.KEY_OAUTH2_ISSUER + "=https://unit.test.kumite",
		IKumiteOAuth2Constants.KEY_JWT_SIGNINGKEY + "=GENERATE" })
@Import({ RedisTestConfiguration.class })
@Slf4j
public class TestSpringAutonomyHerokuRedis implements IKumiteSpringProfiles {

	@Autowired
	ApplicationContext appContest;

	@Autowired
	ObjectMapper objectMapper;

	@Test
	public void testSpringProfiles() {
		log.info("startUpdate: {}", appContest.getStartupDate());
	}

	@Test
	public void testObjectMapper() {
		// Ensure Redis did not corrupted ObjectMapper with its own configuration
		KumiteUserRawRaw rawRaw = KumiteUserRawRaw.builder().providerId("someProviderId").sub("someSub").build();
		@SuppressWarnings("rawtypes")
		Map asMap = objectMapper.convertValue(rawRaw, Map.class);
		Assertions.assertThat(asMap).hasSize(2);
	}

	@Test
	public void testListGames() {

	}
}
