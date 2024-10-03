package eu.solven.kumite.websocket.sse;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.solven.kumite.events.IKumiteContestEvent;
import eu.solven.kumite.websocket.ContestsEventPublisher;
import reactor.core.publisher.Flux;

// https://github.com/oktadev/okta-spring-webflux-react-example/blob/react-app/reactive-web/src/main/java/com/example/demo/ServerSentEventController.java
@RestController
public class ServerSentEventController {
	private final Flux<IKumiteContestEvent> events;
	private final ObjectMapper objectMapper;

	public ServerSentEventController(ContestsEventPublisher eventPublisher, ObjectMapper objectMapper) {
		this.events = Flux.create(eventPublisher).share();
		this.objectMapper = objectMapper;
	}

	// https://blog.ght1pc9kc.fr/2021/server-sent-event-vs-websocket-avec-spring-webflux/
	@GetMapping(path = "/sse/profiles", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	// @CrossOrigin(origins = "http://localhost:3000")
	public Flux<String> profiles() {
		return this.events.map(pce -> {
			try {
				return objectMapper.writeValueAsString(pce) + "\n\n";
			} catch (JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		});
	}
}