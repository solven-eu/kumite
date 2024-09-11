package eu.solven.kumite.app;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import eu.solven.kumite.contest.ContestSearchParameters;
import eu.solven.kumite.contest.ContestView;
import eu.solven.kumite.game.GameSearchParameters;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Configuration
@Import({

		KumitePlayerRandomConfiguration.class,

})
@Slf4j
public class KumitePlayerComponentsConfiguration {

	@Bean
	public IKumiteServer kumiteServer(Environment env) {
		return new KumiteWebclientServer(env);
	}

	@Bean
	public Void playTicTacToe(IKumiteServer kumiteServer, Environment env) {
		// UUID playerId = UUID.fromString(env.getRequiredProperty(null, env.cl))
		UUID playerId = env.getRequiredProperty("kumite.playerId", UUID.class);

		ScheduledExecutorService ses = Executors.newScheduledThreadPool(4);

		Map<UUID, ContestView> contestToDetails = new ConcurrentHashMap<>();
		Set<UUID> playingContests = new ConcurrentSkipListSet<>();

		Stream.of("Travelling Salesman Problem", "Tic-Tac-Toe").forEach(gameTitle -> {
			ses.scheduleWithFixedDelay(() -> {
				log.info("Looking for interesting contests for game LIKE `{}`", gameTitle);
				kumiteServer.searchGames(GameSearchParameters.builder().titleRegex(Optional.of(gameTitle)).build())
						.collectList()
						.flatMapMany(games -> {
							log.info("Games for `{}`: {}", gameTitle, games);
							return Flux.fromStream(games.stream());
						})
						.flatMap(game -> kumiteServer.searchContests(
								ContestSearchParameters.builder().gameId(Optional.of(game.getGameId())).build()))
						.flatMap(contest -> kumiteServer.loadBoard(contest.getContestId(), null))
						.filter(c -> !c.getDynamicMetadata().isGameOver())
						.filter(c -> c.getDynamicMetadata().isAcceptingPlayers())
						.doOnNext(contestView -> {
							UUID contestId = contestView.getContestId();
							log.info("Received board for contestId={}", contestId);

							if (contestView.getPlayerHasJoined()) {
								playingContests.add(contestId);
								contestToDetails.put(contestId, contestView);
							} else if (contestView.getPlayerCanJoin()) {
								kumiteServer.joinContest(playerId, contestId);
							}

						})
						.subscribe(view -> {
							log.info("View: {}", view);
						});
			}, 1, 60, TimeUnit.SECONDS);
		});

		return null;
	}

}
