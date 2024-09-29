package eu.solven.kumite.eventbus;

import org.greenrobot.eventbus.Subscribe;

import eu.solven.kumite.events.PlayerMoved;
import lombok.extern.slf4j.Slf4j;

/**
 * This is responsible for logging main operations.
 * 
 * @author Benoit Lacelle
 *
 */
@Slf4j
public class ActivityLogger implements IEventSubscriber {
	@Subscribe
	public void onPlayerMoved(PlayerMoved playerMoved) {
		log.debug("PlayerMoved: {}", playerMoved);
	}
}
