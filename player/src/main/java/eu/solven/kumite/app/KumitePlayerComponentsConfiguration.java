package eu.solven.kumite.app;

import java.text.ParseException;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

import com.nimbusds.jwt.SignedJWT;

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
		Set<UUID> playerIds = playerIdFromAccessToken(env);

		// UUID playerId = env.getRequiredProperty("kumite.playerId", UUID.class);

		ScheduledExecutorService ses = Executors.newScheduledThreadPool(4);

		for (UUID playerId : playerIds) {
			log.info("Scheduling contest playing for playerId={}", playerId);

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
		}

		return null;
	}

	Set<UUID> playerIdFromAccessToken(Environment env) {
		String accessToken = env.getRequiredProperty("kumite.server.access_token");

		if ("NEEDS_A_PROPER_VALUE".equals(accessToken)) {
			throw new IllegalStateException("Needs to define properly 'kumite.server.access_token'");
		}

		try {
			SignedJWT signed = SignedJWT.parse(accessToken);

			NavigableSet<String> rawPlayerId = new TreeSet<>(signed.getJWTClaimsSet().getStringListClaim("playerIds"));

			return rawPlayerId.stream().map(UUID::fromString).collect(Collectors.toCollection(TreeSet::new));
		} catch (ParseException e) {
			throw new IllegalArgumentException("Issue parsing the access_token", e);
		}
	}

}
