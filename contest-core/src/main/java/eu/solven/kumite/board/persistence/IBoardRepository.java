package eu.solven.kumite.board.persistence;

import java.util.Optional;
import java.util.UUID;

import eu.solven.kumite.board.IKumiteBoard;

public interface IBoardRepository {

	/**
	 * 
	 * @param contestId
	 * @param board
	 * @return if alreadyIn, the already present value
	 */
	Optional<IKumiteBoard> putIfAbsent(UUID contestId, IKumiteBoard board);

	boolean hasContest(UUID contestId);

	Optional<IKumiteBoard> getBoard(UUID contestId);

	void updateBoard(UUID contestId, IKumiteBoard board);

}
