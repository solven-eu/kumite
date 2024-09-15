package eu.solven.kumite.app.persistence;

import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import eu.solven.kumite.app.IKumiteSpringProfiles;
import eu.solven.kumite.board.persistence.InMemoryBoardRepository;
import eu.solven.kumite.player.persistence.InMemoryAccountPlayersRegistry;
import eu.solven.kumite.user.InMemoryUserRepository;

@Import({

		InMemoryUserRepository.class,

		InMemoryAccountPlayersRegistry.class,

		InMemoryBoardRepository.class,

})
@Profile("!" + IKumiteSpringProfiles.P_REDIS)
public class InMemoryKumiteConfiguration {

}
