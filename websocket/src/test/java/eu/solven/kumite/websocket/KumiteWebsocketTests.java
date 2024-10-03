package eu.solven.kumite.websocket;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.socket.CloseStatus;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.solven.kumite.account.fake_player.FakePlayer;
import eu.solven.kumite.app.EmptySpringBootApplication;
import eu.solven.kumite.app.IKumiteSpringProfiles;
import eu.solven.kumite.app.KumiteJackson;
import eu.solven.kumite.app.KumiteServerComponentsConfiguration;
import eu.solven.kumite.board.BoardLifecycleManager;
import eu.solven.kumite.contest.ActiveContestGenerator;
import eu.solven.kumite.contest.Contest;
import eu.solven.kumite.events.ContestIsGameover;
import eu.solven.kumite.events.PlayerCanMove;
import eu.solven.kumite.game.GameMetadata;
import eu.solven.kumite.game.GameSearchParameters;
import eu.solven.kumite.game.GamesRegistry;
import eu.solven.kumite.game.IGameMetadataConstants;
import eu.solven.kumite.oauth2.IKumiteOAuth2Constants;
import eu.solven.kumite.oauth2.authorizationserver.KumiteTokenService;
import eu.solven.kumite.oauth2.resourceserver.KumiteResourceServerConfiguration;
import eu.solven.kumite.player.PlayerJoinRaw;
import eu.solven.kumite.randomgamer.FakeGamer;
import eu.solven.kumite.randomgamer.GamerLogicHelper;
import eu.solven.kumite.security.JwtWebFluxSecurity;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = { EmptySpringBootApplication.class },
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {

		KumiteServerComponentsConfiguration.class,

		// Provide a ReactiveJwtDecoder
		KumiteResourceServerConfiguration.class,

		JwtWebFluxSecurity.class,
		// SocialWebFluxSecurity.class,
		KumiteTokenService.class,

		// Contest generation is not done automatically, else it would trigger the whole gamePlay by RandomPlayer while
		// bootstrapping the unitTest class
		// ActiveContestGenerator.class,

		KumiteWebSocketSpringConfig.class,

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
		"websocket",

})
@TestPropertySource(properties = { IKumiteOAuth2Constants.KEY_JWT_SIGNINGKEY + "=GENERATE" })
@Slf4j
public class KumiteWebsocketTests {

	@LocalServerPort
	private int port;

	@Autowired
	ActiveContestGenerator activeContestGenerator;

	@Autowired
	GamesRegistry gamesRegistry;

	@Autowired
	BoardLifecycleManager boardLifecycleManager;

	@Autowired
	FakeGamer fakeGamer;

	@Autowired
	KumiteTokenService kumiteTokenService;

	WebSocketClient webSocketClient = new ReactorNettyWebSocketClient();

	@Test
	public void getGreeting() throws InterruptedException {
		URI uri = URI.create("ws://localhost:%s/ws/contests".formatted(port));

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
			Mono<Void> wsMono = webSocketClient.execute(uri, headers, (WebSocketSession wsSession) -> {

				AtomicInteger pingIndex = new AtomicInteger();
				AtomicLong msgInIndex = new AtomicLong();

				AtomicBoolean gameIsOver = new AtomicBoolean();

				Publisher<WebSocketMessage> out = Flux.interval(Duration.ofSeconds(5))
						.takeWhile(l -> wsSession.isOpen() && !gameIsOver.get())

						.map(l -> Map.of("playerId",
								playerId,
								"message",
								"Hello Kumite. I'm still connected #%s".formatted(pingIndex.getAndIncrement())))

						.map(map -> {
							try {
								return wsSession.textMessage(objectMapper.writeValueAsString(map));
							} catch (JsonProcessingException e) {
								throw new IllegalArgumentException("Issue writing '" + map + "'", e);
							}
						})
						.doOnNext(msgOut -> {
							log.info("-->({}) {}", wsSession.getId(), msgOut.getPayloadAsText());
						});

				Flux<? extends Map<String, ?>> in = wsSession.receive()
						.takeWhile(l -> wsSession.isOpen() && !gameIsOver.get())
						.map(WebSocketMessage::getPayloadAsText)
						.doOnNext(msgIn -> {
							log.info("<--({}) {}", wsSession.getId(), msgIn);
						})
						.map(msgIn -> {
							Map<String, ?> msgAsObject;
							try {
								msgAsObject = objectMapper.readValue(msgIn, Map.class);
							} catch (JsonProcessingException e) {
								throw new IllegalArgumentException("Issue parsing '" + msgIn + "'", e);
							}

							return msgAsObject;
						})
						.doOnNext(str -> {
							long msgIn = msgInIndex.incrementAndGet();

							if (PlayerCanMove.class.getSimpleName().equals(str.get("eventType"))) {
								fakeGamer.playOnce(contest.getContestId(), playerId);
							} else if (ContestIsGameover.class.getSimpleName().equals(str.get("eventType"))) {
								log.info("The contest is gameOver. Closing {}", wsSession.getId());
								wsSession.close(CloseStatus.NORMAL);
								gameIsOver.set(true);
								// throw new IllegalStateException("Interupt the flux");
							} else {
								log.debug("Msg in: {}", msgIn);
							}
						});

				Mono<Void> sentOut = wsSession.send(out);

				// Zip in and out as in is an infinite stream, including ping every N seconds.
				return Mono.zip(in.then(), sentOut).then();

			});

			// Register the player after connecting the websocket
			boardLifecycleManager.registerPlayer(contest,
					PlayerJoinRaw.builder().contestId(contest.getContestId()).playerId(playerId).build());

			return wsMono;
		}).collect(Collectors.toList());

		Mono.zip(monos.get(0), monos.get(1)).block(Duration.ofMinutes(1));

		Assertions.assertThat(contest.getGame().makeDynamicGameover(contest.getBoard()).isGameOver()).isTrue();
	}
}