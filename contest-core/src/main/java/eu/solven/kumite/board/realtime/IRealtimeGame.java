package eu.solven.kumite.board.realtime;

import java.time.Duration;

import eu.solven.kumite.board.IKumiteBoard;

public interface IRealtimeGame extends IHasRealtimeGame {

	@Override
	default IRealtimeGame getRealtimeGame() {
		return this;
	}

	Duration getPace();

	IKumiteBoard forward(IKumiteBoard board, int nbFrameForward);

}
