package eu.solven.kumite.app;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

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

	@Test
	public void testSpringProfiles() {
		log.info("startUpdate: {}", appContest.getStartupDate());
	}
}
