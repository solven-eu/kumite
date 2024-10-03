package eu.solven.kumite.websocket.stomp;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import eu.solven.kumite.eventbus.IEventSubscriber;
import eu.solven.kumite.events.ContestIsCreated;
import eu.solven.kumite.events.ContestIsGameover;
import eu.solven.kumite.events.PlayerJoinedBoard;
import eu.solven.kumite.events.PlayerMoved;
import lombok.AllArgsConstructor;

@Deprecated(since = "SimpMessagingTemplate would be provided by @EnableWebSocketMessageBroker")
@Service
@AllArgsConstructor
public class StompPushEventBusMessagesToWebsocket implements InitializingBean, IEventSubscriber {

	final EventBus eventBus;
	final SimpMessagingTemplate simpMessagingTemplate;

	@Override
	public void afterPropertiesSet() throws Exception {
		eventBus.register(this);
	}

	@Subscribe
	public void onContestIsCreated(ContestIsCreated contestIsCreated) {
		simpMessagingTemplate.convertAndSend("/topic/contests", contestIsCreated);
	}

	@Subscribe
	public void onContestIsGameover(ContestIsGameover contestIsGameover) {
		simpMessagingTemplate.convertAndSend("/topic/contests", contestIsGameover);
	}

	@Subscribe
	public void onPlayerJoinedBoard(PlayerJoinedBoard playerJoined) {
		simpMessagingTemplate.convertAndSend("/topic/contest/" + playerJoined.getContestId(), playerJoined);
	}

	@Subscribe
	public void onPlayerMoved(PlayerMoved playerMoved) {
		simpMessagingTemplate.convertAndSend("/topic/contest/" + playerMoved.getContestId(), playerMoved);
	}
}