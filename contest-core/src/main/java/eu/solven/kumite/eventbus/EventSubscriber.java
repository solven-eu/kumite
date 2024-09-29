package eu.solven.kumite.eventbus;

import org.greenrobot.eventbus.EventBus;
import org.springframework.beans.factory.InitializingBean;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class EventSubscriber implements IEventSubscriber, InitializingBean {
	final EventBus eventBus;
	final Object subscriber;

	@Override
	public void afterPropertiesSet() throws Exception {
		if (subscriber instanceof InitializingBean) {
			((InitializingBean) subscriber).afterPropertiesSet();
		}

		eventBus.register(subscriber);
	}

}
