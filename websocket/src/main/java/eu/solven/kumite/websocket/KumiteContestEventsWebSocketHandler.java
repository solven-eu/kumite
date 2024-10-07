package eu.solven.kumite.websocket;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.solven.kumite.app.webflux.api.KumiteHandlerHelper;
import eu.solven.kumite.events.IKumiteContestEvent;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Knows how to push {@link IKumiteContestEvent} into a {@link WebSocketSession}
 * 
 * @author Benoit Lacelle
 *
 */
// https://www.baeldung.com/spring-5-reactive-websockets
// https://github.com/eugenp/tutorials/blob/master/spring-reactive-modules/spring-reactive-3/src/main/java/com/baeldung/websocket/ReactiveWebSocketHandler.java
// https://github.com/oktadev/okta-spring-webflux-react-example/blob/react-app/reactive-web/src/main/java/com/example/demo/ServerSentEventController.java
@Component
@Slf4j
public class KumiteContestEventsWebSocketHandler implements WebSocketHandler {

	final ObjectMapper objectMapper;
	final ContestsEventPublisher contestEventsPublisher;

	final Flux<IKumiteContestEvent> sharedFlux;

	public KumiteContestEventsWebSocketHandler(ObjectMapper objectMapper,
			ContestsEventPublisher contestEventsPublisher) {
		this.objectMapper = objectMapper;
		this.contestEventsPublisher = contestEventsPublisher;

		sharedFlux = Flux.create(contestEventsPublisher)
				// `.share` is crucial as each event is published only once (through all Flux)
				.share();
	}

	@Override
	public Mono<Void> handle(WebSocketSession wsSession) {
		AtomicReference<UUID> playerId = new AtomicReference<>();

		Flux<WebSocketMessage> messageFlux = sharedFlux.map(event -> {
			try {
				UUID contestId = event.getContestId();
				Map<String, Object> data = new HashMap<>();
				data.put("eventType", event.getClass().getSimpleName());
				data.put("contestId", contestId);
				return objectMapper.writeValueAsString(data);
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		}).map(messageAsString -> {
			log.info("--> Sending {} to {}", messageAsString, wsSession.getId());
			return wsSession.textMessage(messageAsString);
		});

		Mono<Void> out = wsSession.send(messageFlux);
		Flux<?> in = wsSession.receive().map(WebSocketMessage::getPayloadAsText).map(messageAsString -> {
			log.info("<-- Receiving {} from {}", messageAsString, wsSession.getId());
			return messageAsString;
		}).map(msgAsString -> {
			try {
				return objectMapper.readValue(msgAsString, Map.class);
			} catch (JsonProcessingException e) {
				// TODO SHould we send it back as out?
				throw new IllegalArgumentException("Invalid json: `%s`".formatted(msgAsString), e);
			}
		}).doOnNext(asMap -> {
			String keyPlayerId = "playerId";
			if (asMap.containsKey(keyPlayerId)) {
				UUID receivedPlayerId = KumiteHandlerHelper.uuid((String) asMap.get(keyPlayerId), keyPlayerId);
				log.info("wsId={} has registered playerId={}", wsSession.getId(), receivedPlayerId);
				playerId.set(receivedPlayerId);
			}
		});
		return Flux.zip(in.then(), out.flux()).then().doOnError(t -> {
			log.warn("Arg in wsId={}", wsSession.getId(), t);
		});
	}

}