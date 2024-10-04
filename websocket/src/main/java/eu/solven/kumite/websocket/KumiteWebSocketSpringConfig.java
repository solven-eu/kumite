package eu.solven.kumite.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Import({

		KumiteContestEventsWebSocketHandler.class,
		KumiteWebsocketHandlerMapping.class,

		ContestsEventPublisher.class,

})
@AllArgsConstructor
@Slf4j
public class KumiteWebSocketSpringConfig {

}
