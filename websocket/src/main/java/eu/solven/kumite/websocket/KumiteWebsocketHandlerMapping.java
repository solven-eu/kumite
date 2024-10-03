package eu.solven.kumite.websocket;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class KumiteWebsocketHandlerMapping {
	@Autowired
	private WebSocketHandler webSocketHandler;

	@Bean
	public HandlerMapping webSocketHandlerMapping() {
		Map<String, WebSocketHandler> map = new HashMap<>();

		String contestEventsRoute = "/ws" + "/contests";
		log.info("Register `{}` -> {}", contestEventsRoute, webSocketHandler.getClass().getSimpleName());
		map.put(contestEventsRoute, webSocketHandler);

		int order = 1;
		return new SimpleUrlHandlerMapping(map, order);
	}
}
