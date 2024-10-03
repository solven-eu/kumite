package eu.solven.kumite.websocket;

import java.time.Duration;

import org.greenrobot.eventbus.EventBus;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketMessage.Type;
import org.springframework.web.reactive.socket.WebSocketSession;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.solven.kumite.account.login.IKumiteTestConstants;
import eu.solven.kumite.app.KumiteJackson;
import eu.solven.kumite.events.PlayerCanMove;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
@Import({

		KumiteContestEventsWebSocketHandler.class,

		ContestsEventPublisher.class,

		TestEventMulticast.Complement.class,

})
public class TestEventMulticast implements IKumiteTestConstants {

	public static class Complement {
		@Bean
		EventBus eventBus() {
			return EventBus.builder().build();
		}

		@Bean
		ObjectMapper objectMapper() {
			return KumiteJackson.objectMapper();
		}
	}

	@Autowired
	KumiteContestEventsWebSocketHandler wsHandler;

	@Autowired
	EventBus eventBus;

	@Disabled("Need to grow stronger in Reactor")
	@Test
	public void testEnsureMulticast() throws InterruptedException {
		WebSocketSession session1 = Mockito.mock(WebSocketSession.class);
		WebSocketSession session2 = Mockito.mock(WebSocketSession.class);

		Mockito.when(session1.getId()).thenReturn("ws_1");
		Mockito.when(session1.receive()).thenReturn(Flux.empty());
		Mockito.when(session1.textMessage(Mockito.anyString()))
				.thenReturn(new WebSocketMessage(Type.TEXT, Mockito.mock(DataBuffer.class)));
		Mockito.when(session1.send(Mockito.any(Publisher.class))).thenAnswer(invok -> {
			Publisher<?> publisher = invok.getArgument(0, Publisher.class);

			return Mono.from(publisher).then();
		});

		Mockito.when(session2.getId()).thenReturn("ws_2");
		Mockito.when(session2.receive()).thenReturn(Flux.empty());
		Mockito.when(session2.textMessage(Mockito.anyString()))
				.thenReturn(new WebSocketMessage(Type.TEXT, Mockito.mock(DataBuffer.class)));

		Mockito.when(session2.send(Mockito.any(Publisher.class))).thenAnswer(invok -> {
			Publisher<?> publisher = invok.getArgument(0, Publisher.class);

			return Mono.from(publisher).then();
		});

		Mono<Void> mono1 = wsHandler.handle(session1);
		Mono<Void> mono2 = wsHandler.handle(session2);

		eventBus.post(PlayerCanMove.builder().contestId(someContestId).playerId(somePlayerId).build());

		// mono1.subscribe();

		mono1.block(Duration.ofSeconds(5));
		mono2.block(Duration.ofSeconds(5));
		// mono2.subscribe();
		Mockito.verify(session1)
				.textMessage(
						Mockito.contains("{\"contestId\":\"" + someContestId + "\",\"eventType\":\"PlayerCanMove\"}"));

		Mockito.verify(session2)
				.textMessage(
						Mockito.contains("{\"contestId\":\"" + someContestId + "\",\"eventType\":\"PlayerCanMove\"}"));

		mono2.block();
	}
}
