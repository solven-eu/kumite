package eu.solven.kumite.board;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BoardsRegistry {
	final Map<UUID, IKumiteBoard> contestIdToBoard = new ConcurrentHashMap<>();

	public void registerBoard(UUID contestId, IKumiteBoard initialBoard) {
		IKumiteBoard alreadyIn = contestIdToBoard.putIfAbsent(contestId, initialBoard);
		if (alreadyIn != null) {
			throw new IllegalArgumentException(
					"board already registered (" + alreadyIn + ") for contestId=" + contestId);
		}
	}

	public IHasBoard makeDynamicBoardHolder(UUID contestId) {
		if (!contestIdToBoard.containsKey(contestId)) {
			throw new IllegalArgumentException("Unknown contestId=" + contestId);
		}

		return () -> contestIdToBoard.get(contestId);
	}

	public void updateBoard(UUID contestId, IKumiteBoard currentBoard) {
		contestIdToBoard.put(contestId, currentBoard);
	}
}
