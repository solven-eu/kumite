package eu.solven.kumite.board.persistence;

import java.util.Optional;
import java.util.UUID;

import eu.solven.kumite.board.IKumiteBoard;

public interface IBoardRepository {

	IKumiteBoard putIfAbsent(UUID contestId, IKumiteBoard initialBoard);

	boolean containsKey(UUID contestId);

	Optional<IKumiteBoard> getBoard(UUID contestId);

	void updateBoard(UUID contestId, IKumiteBoard currentBoard);

}
