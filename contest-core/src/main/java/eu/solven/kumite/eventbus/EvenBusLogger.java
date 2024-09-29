package eu.solven.kumite.eventbus;

import java.util.logging.Level;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Logger;
import org.greenrobot.eventbus.NoSubscriberEvent;
import org.greenrobot.eventbus.Subscribe;

import lombok.extern.slf4j.Slf4j;

/**
 * Transcode from JUL to SLF4J for {@link EventBus}
 * 
 * @author Benoit Lacelle
 *
 */
@Slf4j
public class EvenBusLogger implements Logger {

	@Override
	public void log(Level level, String msg) {
		if (level == Level.SEVERE) {
			log.error("{}", msg);
		} else if (level == Level.WARNING) {
			log.warn("{}", msg);
		} else if (level == Level.INFO) {
			log.info("{}", msg);
		} else if (level == Level.FINE) {
			log.debug("{}", msg);
		} else if (level == Level.FINER || level == Level.FINEST) {
			log.trace("{}", msg);
		} else {
			log.error("Unmanaged level={}. Original message: {}", level, msg);
		}
	}

	@Override
	public void log(Level level, String msg, Throwable t) {
		if (level == Level.SEVERE) {
			log.error("{}", msg, t);
		} else if (level == Level.WARNING) {
			log.warn("{}", msg, t);
		} else if (level == Level.INFO) {
			log.info("{}", msg, t);
		} else if (level == Level.FINE) {
			log.debug("{}", msg, t);
		} else if (level == Level.FINER || level == Level.FINEST) {
			log.trace("{}", msg, t);
		} else {
			log.error("Unmanaged level={}. Original message: {}", level, msg, t);
		}
	}

	@Subscribe
	public void onNoSubscriberEvent(NoSubscriberEvent noSubscriberEvent) {
		log.warn("No subscriberEvent for {}", noSubscriberEvent.originalEvent);
	}
}
