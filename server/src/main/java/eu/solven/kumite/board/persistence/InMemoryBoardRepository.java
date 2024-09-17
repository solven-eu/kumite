package eu.solven.kumite.board.persistence;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import eu.solven.kumite.board.IKumiteBoard;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class InMemoryBoardRepository implements IBoardRepository {
	final Map<UUID, IKumiteBoard> contestIdToBoard = new ConcurrentHashMap<>();

	@Override
	public Optional<IKumiteBoard> putIfAbsent(UUID contestId, IKumiteBoard initialBoard) {
		IKumiteBoard alreadyIn = contestIdToBoard.putIfAbsent(contestId, initialBoard);
		return Optional.ofNullable(alreadyIn);
	}

	@Override
	public boolean containsKey(UUID contestId) {
		return contestIdToBoard.containsKey(contestId);
	}

	@Override
	public Optional<IKumiteBoard> getBoard(UUID contestId) {
		return Optional.ofNullable(contestIdToBoard.get(contestId));
	}

	@Override
	public void updateBoard(UUID contestId, IKumiteBoard currentBoard) {
		IKumiteBoard previousBoard = contestIdToBoard.put(contestId, currentBoard);
		if (previousBoard == null) {
			throw new IllegalStateException("The board was not already present");
		}
	}
}
