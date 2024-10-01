package eu.solven.kumite.randomgamer;

import java.util.List;

import org.greenrobot.eventbus.EventBus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import eu.solven.kumite.account.internal.KumiteUser;
import eu.solven.kumite.app.IKumiteSpringProfiles;
import eu.solven.kumite.contest.ActiveContestGenerator;
import eu.solven.kumite.eventbus.EventSubscriber;
import eu.solven.kumite.eventbus.IEventSubscriber;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Profile(IKumiteSpringProfiles.P_RANDOM_PLAYS_VSTHEMSELVES)
@Import({

		ActiveContestGenerator.class,

		RandomGamer.class,

})
@Slf4j
public class RandomPlaysVsThemselvesConfig {

	@Bean
	public IEventSubscriber playsRandomly(RandomGamer randomGamer, EventBus eventBus, List<KumiteUser> players) {
		log.debug("Now {} are registered, we can generate events", players);

		IContestJoiningStrategy strategy = new RandomPlayersVsThemselves();
		log.info("Registering {} into {}", strategy, randomGamer);

		IEventSubscriber randomSubscribeToEvents =
				new RandomPlaysVs1Config.RandomPlayerEventSubscriber(randomGamer, strategy);

		return new EventSubscriber(eventBus, randomSubscribeToEvents);
	}
}
