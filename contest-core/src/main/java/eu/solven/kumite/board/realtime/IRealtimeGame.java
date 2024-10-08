package eu.solven.kumite.board.realtime;

import java.time.Duration;
import java.util.random.RandomGenerator;

import eu.solven.kumite.board.IKumiteBoard;

public interface IRealtimeGame extends IHasRealtimeGame {

	@Override
	default IRealtimeGame getRealtimeGame() {
		return this;
	}

	Duration getPace();

	IKumiteBoard forward(RandomGenerator randomGenerator, IKumiteBoard board, int nbFrameForward);

}
