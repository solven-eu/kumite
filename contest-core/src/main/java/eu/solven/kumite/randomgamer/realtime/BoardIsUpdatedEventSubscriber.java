package eu.solven.kumite.randomgamer.realtime;

import java.util.function.Consumer;

import org.greenrobot.eventbus.Subscribe;

import eu.solven.kumite.events.BoardIsUpdated;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class BoardIsUpdatedEventSubscriber {
	final Consumer<BoardIsUpdated> onBoardUpdated;

	@Subscribe
	public void onBoardIsUpdated(BoardIsUpdated event) {
		onBoardUpdated.accept(event);
	}
}
