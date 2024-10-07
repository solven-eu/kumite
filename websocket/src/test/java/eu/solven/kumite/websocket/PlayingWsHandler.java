package eu.solven.kumite.websocket;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.reactivestreams.Publisher;
import org.springframework.web.reactive.socket.CloseStatus;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.solven.kumite.contest.Contest;
import eu.solven.kumite.events.ContestIsGameover;
import eu.solven.kumite.events.PlayerCanMove;
import eu.solven.kumite.randomgamer.turnbased.FakeTurnBasedGamer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Slf4j
public class PlayingWsHandler implements WebSocketHandler {
	final ObjectMapper objectMapper;
	final Contest contest;
	final UUID playerId;
	final FakeTurnBasedGamer fakeGamer;

	@Override
	public Mono<Void> handle(WebSocketSession session) {
		AtomicInteger pingIndex = new AtomicInteger();
		AtomicLong msgInIndex = new AtomicLong();

		AtomicBoolean gameIsOver = new AtomicBoolean();

		Publisher<WebSocketMessage> out = Flux.interval(Duration.ofSeconds(5))
				.takeWhile(l -> session.isOpen() && !gameIsOver.get())

				.map(l -> Map.of("playerId",
						playerId,
						"message",
						"Hello Kumite. I'm still connected #%s".formatted(pingIndex.getAndIncrement())))

				.map(map -> {
					try {
						return session.textMessage(objectMapper.writeValueAsString(map));
					} catch (JsonProcessingException e) {
						throw new IllegalArgumentException("Issue writing '" + map + "'", e);
					}
				})
				.doOnNext(msgOut -> {
					log.info("-->({}) {}", session.getId(), msgOut.getPayloadAsText());
				});

		Flux<? extends Map<String, ?>> in = session.receive()
				.takeWhile(l -> session.isOpen() && !gameIsOver.get())
				.map(WebSocketMessage::getPayloadAsText)
				.doOnNext(msgIn -> {
					log.info("<--({}) {}", session.getId(), msgIn);
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
						log.info("The contest is gameOver. Closing {}", session.getId());
						session.close(CloseStatus.NORMAL);
						gameIsOver.set(true);
					} else {
						log.debug("Msg in: {}", msgIn);
					}
				});

		Mono<Void> sentOut = session.send(out);

		// Zip in and out as in is an infinite stream, including ping every N seconds.
		return Mono.zip(in.then(), sentOut).then();

	}

}
