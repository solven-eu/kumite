package eu.solven.kumite.randomgamer;

import java.util.List;
import java.util.function.Predicate;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import eu.solven.kumite.account.internal.KumiteUser;
import eu.solven.kumite.app.IKumiteSpringProfiles;
import eu.solven.kumite.contest.ActiveContestGenerator;
import eu.solven.kumite.contest.ContestSearchParameters;
import eu.solven.kumite.eventbus.EventSubscriber;
import eu.solven.kumite.eventbus.IEventSubscriber;
import eu.solven.kumite.events.ContestIsCreated;
import eu.solven.kumite.events.PlayerJoinedBoard;
import eu.solven.kumite.events.PlayerMoved;
import eu.solven.kumite.player.KumitePlayer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Profile(IKumiteSpringProfiles.P_RANDOM_PLAYS_VS1)
@Import({

		ActiveContestGenerator.class,

		RandomGamer.class,

})
@Slf4j
public class RandomPlaysVs1Config {

	@AllArgsConstructor
	public static class RandomPlayerEventSubscriber implements IEventSubscriber {
		final RandomGamer randomGamer;
		final IContestJoiningStrategy strategy;

		@Subscribe
		public void onContestIsCreated(ContestIsCreated contestIsCreated) {
			// Try joining as soon as the contest is created
			randomGamer.joinOncePerContestAndPlayer(
					ContestSearchParameters.searchContestId(contestIsCreated.getContestId()),
					strategy);
		}

		@Subscribe
		public void onPlayerJoinedBoard(PlayerJoinedBoard playerJoined) {
			ContestSearchParameters searchContestId =
					ContestSearchParameters.searchContestId(playerJoined.getContestId());

			// Try playing right away
			randomGamer.playOncePerContestAndPlayer(searchContestId, p -> true);
		}

		@Subscribe
		public void onBoardIsUpdated(PlayerMoved playerMoved) {
			ContestSearchParameters searchContestId =
					ContestSearchParameters.searchContestId(playerMoved.getContestId());
			Predicate<KumitePlayer> playOtherPlayers = p -> !p.getPlayerId().equals(playerMoved.getPlayerId());

			// Try playing right away
			randomGamer.playOncePerContestAndPlayer(searchContestId, playOtherPlayers);
		}

	};

	@Bean
	public IEventSubscriber playsRandomly(RandomGamer randomGamer, EventBus eventBus, List<KumiteUser> players) {
		log.debug("Now {} are registered, we can generate events", players);

		IContestJoiningStrategy strategy = new RandomPlayersVs1();
		log.info("Registering {} into {}", strategy, randomGamer);

		IEventSubscriber randomSubscribeToEvents = new RandomPlayerEventSubscriber(randomGamer, strategy);

		return new EventSubscriber(eventBus, randomSubscribeToEvents);
	}
}
