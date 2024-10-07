package eu.solven.kumite.board;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import lombok.Getter;

public class EventSink {
	@Getter
	private final List<Object> events = new CopyOnWriteArrayList<>();

	@Subscribe
	public void onEvent(Object event) {
		events.add(event);
	}

	public static EventSink makeEventSink(EventBus eventBus) {
		EventSink sink = new EventSink();

		eventBus.register(sink);

		return sink;
	}
}
