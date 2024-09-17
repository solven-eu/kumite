package eu.solven.kumite.app;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import eu.solven.kumite.app.player.IGamingLogic;
import eu.solven.kumite.app.player.RandomGamingLogic;
import eu.solven.kumite.app.server.IKumiteServer;
import eu.solven.kumite.app.server.KumiteWebclientServer;
import lombok.extern.slf4j.Slf4j;

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
	public IGamingLogic kumitePlayer(IKumiteServer kumiteServer) {
		return new RandomGamingLogic(kumiteServer);
	}

	@Bean
	public Void playGames(IGamingLogic kumitePlayer, Environment env) {
		UUID playerId = env.getRequiredProperty("kumite.playerId", UUID.class);

		ScheduledExecutorService ses = Executors.newScheduledThreadPool(4);

		ses.scheduleWithFixedDelay(() -> {
			try {
				log.info("Playing contests as {}", playerId);
				kumitePlayer.playOptimizationGames(playerId);
			} catch (Throwable t) {
				log.warn("Issue while playing games", t);
			}

		}, 1, 60, TimeUnit.SECONDS);

		ses.scheduleWithFixedDelay(() -> {
			try {
				log.info("Playing contests as {}", playerId);
				kumitePlayer.play1v1TurnBasedGames(playerId);
			} catch (Throwable t) {
				log.warn("Issue while playing games", t);
			}

		}, 1, 60, TimeUnit.SECONDS);

		return null;
	}


}
