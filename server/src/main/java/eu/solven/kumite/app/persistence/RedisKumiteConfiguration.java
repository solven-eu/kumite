package eu.solven.kumite.app.persistence;

import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import eu.solven.kumite.app.IKumiteSpringProfiles;
import eu.solven.kumite.board.persistence.RedisBoardRepository;
import eu.solven.kumite.contest.persistence.RedisContestRepository;
import eu.solven.kumite.player.persistence.BijectiveAccountPlayersRegistry;
import eu.solven.kumite.user.InMemoryUserRepository;

@Import({

		InMemoryUserRepository.class,

		BijectiveAccountPlayersRegistry.class,

		RedisBoardRepository.class,
		RedisContestRepository.class,

})
@Profile(IKumiteSpringProfiles.P_REDIS)
public class RedisKumiteConfiguration {

}
