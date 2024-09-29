package eu.solven.kumite.app.persistence;

import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import eu.solven.kumite.app.IKumiteSpringProfiles;
import eu.solven.kumite.board.persistence.RedisBoardRepository;
import eu.solven.kumite.contest.persistence.RedisContestRepository;
import eu.solven.kumite.player.persistence.BijectiveAccountPlayersRegistry;
import eu.solven.kumite.redis.KumiteRawRedisConfiguration;
import eu.solven.kumite.user.RedisUserRepository;

/**
 * Enables relying on Redis to persiste data.
 * 
 * @author Benoit Lacelle
 *
 */
@Import({

		RedisUserRepository.class,

		BijectiveAccountPlayersRegistry.class,

		RedisBoardRepository.class,
		RedisContestRepository.class,

		KumiteRawRedisConfiguration.class,

})
@Profile(IKumiteSpringProfiles.P_REDIS)
public class RedisKumiteConfiguration {

}
