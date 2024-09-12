package eu.solven.kumite.app;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import eu.solven.kumite.app.player.IKumitePlayer;
import eu.solven.kumite.app.player.KumitePlayer;
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
	public IKumitePlayer kumitePlayer(IKumiteServer kumiteServer) {
		return new KumitePlayer(kumiteServer);
	}

	@Bean
	public Void playTicTacToe(IKumitePlayer kumitePlayer, Environment env) {
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
				kumitePlayer.play1v1(playerId);
			} catch (Throwable t) {
				log.warn("Issue while playing games", t);
			}

		}, 1, 60, TimeUnit.SECONDS);

		return null;
	}


}
