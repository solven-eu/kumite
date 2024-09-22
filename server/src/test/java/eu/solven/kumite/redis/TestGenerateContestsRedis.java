package eu.solven.kumite.redis;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import eu.solven.kumite.app.IKumiteSpringProfiles;
import eu.solven.kumite.app.KumiteServerComponentsConfiguration;
import eu.solven.kumite.contest.ActiveContestGenerator;
import eu.solven.kumite.contest.persistence.IContestsRepository;
import eu.solven.kumite.game.GamesRegistry;
import eu.solven.kumite.oauth2.IKumiteOAuth2Constants;
import lombok.extern.slf4j.Slf4j;

@ExtendWith(SpringExtension.class)
@ActiveProfiles({ IKumiteSpringProfiles.P_REDIS, IKumiteSpringProfiles.P_INJECT_DEFAULT_GAMES })
@TestPropertySource(properties = {
		// https://github.com/spring-projects/spring-boot/issues/4877
		"rediscloud.url" + "=redis://${spring.data.redis.host}:${spring.data.redis.port}",
		"spring.data.redis.host" + "=localhost",
		"spring.data.redis.port" + "=6380",
		IKumiteOAuth2Constants.KEY_OAUTH2_ISSUER + "=https://unit.test.kumite",
		IKumiteOAuth2Constants.KEY_JWT_SIGNINGKEY + "=GENERATE" })
@Import({ RedisTestConfiguration.class,

		KumiteServerComponentsConfiguration.class,
		RedisAutoConfiguration.class,

})
@Slf4j
public class TestGenerateContestsRedis {

	@Autowired
	RedisTemplate<Object, Object> redisTemplate;

	@Autowired
	GamesRegistry gamesRegistry;

	@Autowired
	IContestsRepository contestRepository;

	@Autowired
	ActiveContestGenerator generator;

	@BeforeEach
	public void clearContests() {
		redisTemplate.scan(ScanOptions.scanOptions().build()).forEachRemaining(k -> redisTemplate.delete(k));
	}

	@Test
	public void testGenerateContestsMultipleTimes() {
		long nbGames = gamesRegistry.getGames().count();

		long countBefore = contestRepository.stream().count();
		Assertions.assertThat(countBefore).isEqualTo(0);

		// This should initialize 1 contest per game
		generator.makeContestsIfNoneJoinable();

		long countMiddle = contestRepository.stream().count();
		Assertions.assertThat(countMiddle).isEqualTo(nbGames);

		// This should be a no-op as previous contests are still joinable
		generator.makeContestsIfNoneJoinable();

		long countAfter = contestRepository.stream().count();
		Assertions.assertThat(countAfter).isEqualTo(nbGames);
	}
}
