package eu.solven.kumite.board.persistence;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import eu.solven.kumite.board.IKumiteBoard;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public class InMemoryBoardRepository implements IBoardRepository {
	final Map<UUID, IKumiteBoard> contestIdToBoard = new ConcurrentHashMap<>();

	@Override
	public Optional<IKumiteBoard> putIfAbsent(UUID contestId, IKumiteBoard initialBoard) {
		IKumiteBoard alreadyIn = contestIdToBoard.putIfAbsent(contestId, initialBoard);
		return Optional.ofNullable(alreadyIn);
	}

	@Override
	public boolean hasContest(UUID contestId) {
		return contestIdToBoard.containsKey(contestId);
	}

	// TODO We should return a clone, to detect earlier lack of persistence (e.g. currently detected with Redis)
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

	public void clear() {
		long size = contestIdToBoard.size();
		log.info("We reset {} boards", size);
		contestIdToBoard.clear();
	}
}
