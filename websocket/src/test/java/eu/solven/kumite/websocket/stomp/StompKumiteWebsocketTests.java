package eu.solven.kumite.websocket.stomp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Type;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
//import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import eu.solven.kumite.app.EmptySpringBootApplication;
import eu.solven.kumite.app.IKumiteSpringProfiles;
import eu.solven.kumite.app.KumiteServerComponentsConfiguration;
import eu.solven.kumite.contest.ActiveContestGenerator;
import eu.solven.kumite.events.ContestIsCreated;
import eu.solven.kumite.greeting.Greeting;
import eu.solven.kumite.leaderboard.rating.PlayerInContest;
import eu.solven.kumite.websocket.KumiteContestEventsWebSocketHandler;
import eu.solven.kumite.websocket.KumiteWebsocketHandlerMapping;

@Disabled("Stomp is not (natively) compatible with Spring Reactive")
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = { EmptySpringBootApplication.class },
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// @EnableWebMvc
@ContextConfiguration(classes = {

		KumiteServerComponentsConfiguration.class,

		// SocialWebFluxSecurity.class,

		// Contest generation is not done automatically, else it would trigger the whole gamePlay by RandomPlayer while
		// bootstrapping the unitTest class
		ActiveContestGenerator.class,

		BoardWebsocketController.class,
		StompPushEventBusMessagesToWebsocket.class,
		StompWebSocketConfig.class,
		WebSocketSecurityConfig.class,

		KumiteContestEventsWebSocketHandler.class,
		KumiteWebsocketHandlerMapping.class,

})
// Fake player plays with the WebSocket, against random players
@ActiveProfiles({ IKumiteSpringProfiles.P_INMEMORY,
		IKumiteSpringProfiles.P_INJECT_DEFAULT_GAMES,
		IKumiteSpringProfiles.P_RANDOM_PLAYS_VS1,
		"websocket", })
public class StompKumiteWebsocketTests {

	@LocalServerPort
	private int port;

	@Autowired
	ActiveContestGenerator activeContestGenerator;

	private WebSocketStompClient stompClient;

	private final WebSocketHttpHeaders headers = new WebSocketHttpHeaders();

	@BeforeEach
	public void setup() {
		WebSocketClient webSocketClient = new StandardWebSocketClient();
		this.stompClient = new WebSocketStompClient(webSocketClient);
		this.stompClient.setMessageConverter(new MappingJackson2MessageConverter());
	}

	@Test
	public void getGreeting() throws InterruptedException {

		final CountDownLatch latch = new CountDownLatch(1);
		final AtomicReference<Throwable> failure = new AtomicReference<>();

		StompSessionHandler handler = new TestSessionHandler(failure) {

			@Override
			public void afterConnected(final StompSession session, StompHeaders connectedHeaders) {
				session.subscribe("/topic/contests", new StompFrameHandler() {
					@Override
					public Type getPayloadType(StompHeaders headers) {
						return ContestIsCreated.class;
					}

					@Override
					public void handleFrame(StompHeaders headers, Object payload) {
						ContestIsCreated greeting = (ContestIsCreated) payload;
						try {
							// assertEquals("Hello, Spring!", greeting.getContent());
						} catch (Throwable t) {
							failure.set(t);
						} finally {
							session.disconnect();
							latch.countDown();
						}
					}
				});
				session.subscribe("/kumite/api/v1/news", new StompFrameHandler() {
					@Override
					public Type getPayloadType(StompHeaders headers) {
						return Greeting.class;
					}

					@Override
					public void handleFrame(StompHeaders headers, Object payload) {
						Greeting greeting = (Greeting) payload;
						try {
							assertEquals("Hello, Spring!", greeting.getMessage());
						} catch (Throwable t) {
							failure.set(t);
						} finally {
							session.disconnect();
							latch.countDown();
						}
					}
				});
				session.subscribe("/kumite/topic/greetings", new StompFrameHandler() {
					@Override
					public Type getPayloadType(StompHeaders headers) {
						return Object.class;
					}

					@Override
					public void handleFrame(StompHeaders headers, Object payload) {
						// Greeting greeting = (Greeting) payload;
						try {
							// assertEquals("Hello, Spring!", greeting.getContent());
						} catch (Throwable t) {
							failure.set(t);
						} finally {
							session.disconnect();
							latch.countDown();
						}
					}
				});

				StompHeaders headers = new StompHeaders();

				headers.set(HttpHeaders.AUTHORIZATION, "Bearer XXX");

				try {
					headers.setDestination("/kumite/contest/player");
					session.send(headers, PlayerInContest.builder().contestId(null));
				} catch (Throwable t) {
					failure.set(t);
					latch.countDown();
				}
				try {
					headers.setDestination("/kumite/api/v1/hello");
					session.send(headers, PlayerInContest.builder().contestId(null));
				} catch (Throwable t) {
					failure.set(t);
					latch.countDown();
				}
			}
		};

		activeContestGenerator.makeContestsIfNoneJoinable();

		this.stompClient.connectAsync("ws://localhost:{port}/event-emitter", this.headers, handler, this.port);

		if (latch.await(3, TimeUnit.MINUTES)) {
			if (failure.get() != null) {
				throw new AssertionError("", failure.get());
			}
		} else {
			fail("Greeting not received");
		}

	}

	private static class TestSessionHandler extends StompSessionHandlerAdapter {

		private final AtomicReference<Throwable> failure;

		public TestSessionHandler(AtomicReference<Throwable> failure) {
			this.failure = failure;
		}

		@Override
		public void handleFrame(StompHeaders headers, Object payload) {
			this.failure.set(new Exception(headers.toString()));
		}

		@Override
		public void handleException(StompSession s, StompCommand c, StompHeaders h, byte[] p, Throwable ex) {
			this.failure.set(ex);
		}

		@Override
		public void handleTransportError(StompSession session, Throwable ex) {
			this.failure.set(ex);
		}
	}
}