package eu.solven.kumite.websocket;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.solven.kumite.account.fake_player.FakePlayer;
import eu.solven.kumite.app.EmptySpringBootApplication;
import eu.solven.kumite.app.IKumiteSpringProfiles;
import eu.solven.kumite.app.KumiteJackson;
import eu.solven.kumite.app.KumiteServerComponentsConfiguration;
import eu.solven.kumite.board.BoardLifecycleManager;
import eu.solven.kumite.board.BoardsRegistry;
import eu.solven.kumite.contest.ActiveContestGenerator;
import eu.solven.kumite.contest.Contest;
import eu.solven.kumite.game.GameMetadata;
import eu.solven.kumite.game.GameSearchParameters;
import eu.solven.kumite.game.GamesRegistry;
import eu.solven.kumite.game.IGameMetadataConstants;
import eu.solven.kumite.oauth2.IKumiteOAuth2Constants;
import eu.solven.kumite.oauth2.authorizationserver.KumiteTokenService;
import eu.solven.kumite.oauth2.resourceserver.JwtWebFluxSecurity;
import eu.solven.kumite.oauth2.resourceserver.KumiteResourceServerConfiguration;
import eu.solven.kumite.player.PlayerJoinRaw;
import eu.solven.kumite.randomgamer.FakeGamer;
import eu.solven.kumite.randomgamer.GamerLogicHelper;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = { EmptySpringBootApplication.class },
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {

		KumiteServerComponentsConfiguration.class,

		// Provide a ReactiveJwtDecoder
		KumiteResourceServerConfiguration.class,

		JwtWebFluxSecurity.class,
		KumiteTokenService.class,

		KumiteWebSocketManualAuthSpringConfig.class,

		GamerLogicHelper.class,
		FakeGamer.class,

})
// Fake player plays with the WebSocket, against random players
@ActiveProfiles({

		IKumiteSpringProfiles.P_INMEMORY,
		IKumiteSpringProfiles.P_UNSAFE_EXTERNAL_OAUTH2,
		IKumiteSpringProfiles.P_UNSAFE_SERVER,
		IKumiteSpringProfiles.P_INJECT_DEFAULT_GAMES,
		IKumiteSpringProfiles.P_FAKEUSER,
		// IKumiteSpringProfiles.P_RANDOM_PLAYS_VS1,
		IKumiteSpringProfiles.P_LOGGING,

})
@TestPropertySource(properties = { IKumiteOAuth2Constants.KEY_JWT_SIGNINGKEY + "=" + IKumiteOAuth2Constants.GENERATE })
@Slf4j
public class KumiteWebsocketTests {

	@LocalServerPort
	int serverPort;
	@Autowired
	ActiveContestGenerator activeContestGenerator;
	@Autowired
	GamesRegistry gamesRegistry;
	@Autowired
	BoardsRegistry boardsRegistry;
	@Autowired
	BoardLifecycleManager boardLifecycleManager;
	@Autowired
	FakeGamer fakeGamer;
	@Autowired
	KumiteTokenService kumiteTokenService;

	WebSocketClient webSocketClient = new ReactorNettyWebSocketClient();

	@Test
	public void getGreeting() throws InterruptedException {
		URI uri = URI.create("ws://localhost:%s/ws/contests".formatted(serverPort));

		GameMetadata turnBased1v1 = gamesRegistry.searchGames(GameSearchParameters.builder()
				.requiredTag(IGameMetadataConstants.TAG_1V1)
				.requiredTag(IGameMetadataConstants.TAG_1V1)
				.build()).get(0);
		log.info("We are going to play {}", turnBased1v1);

		Contest contest = activeContestGenerator.openContestForGame(turnBased1v1.getGameId());

		ObjectMapper objectMapper = KumiteJackson.objectMapper();

		List<Mono<Void>> monos = FakePlayer.playersIds().stream().limit(2).map(playerId -> {
			HttpHeaders headers = new HttpHeaders();

			String jwt = kumiteTokenService
					.generateAccessToken(FakePlayer.ACCOUNT_ID, Set.of(playerId), Duration.ofMinutes(5), false);

			headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwt);

			log.info("Wire WS for playerId={}", playerId);
			PlayingWsHandler wsHandler = new PlayingWsHandler(objectMapper, contest, playerId, fakeGamer);
			Mono<Void> wsMono = webSocketClient.execute(uri, headers, wsHandler);

			// Register the player after connecting the websocket
			boardLifecycleManager.registerPlayer(contest,
					PlayerJoinRaw.builder().contestId(contest.getContestId()).playerId(playerId).build());

			return wsMono;
		}).collect(Collectors.toList());

		Mono.zip(monos.get(0), monos.get(1)).block(Duration.ofMinutes(1));

		Assertions.assertThat(boardsRegistry.hasGameover(contest.getGame(), contest.getContestId()).isGameOver())
				.isTrue();
	}

}