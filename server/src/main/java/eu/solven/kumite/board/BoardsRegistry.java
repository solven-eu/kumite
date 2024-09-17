package eu.solven.kumite.board;

import java.util.Optional;
import java.util.UUID;

import eu.solven.kumite.board.persistence.IBoardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class BoardsRegistry {
	final IBoardRepository boardRepository;

	public void registerBoard(UUID contestId, IKumiteBoard initialBoard) {
		Optional<IKumiteBoard> alreadyIn = boardRepository.putIfAbsent(contestId, initialBoard);
		if (alreadyIn.isPresent()) {
			throw new IllegalArgumentException(
					"board already registered (" + alreadyIn + ") for contestId=" + contestId);
		}
	}

	public IHasBoard makeDynamicBoardHolder(UUID contestId) {
		if (!boardRepository.containsKey(contestId)) {
			throw new IllegalArgumentException("Unknown contestId=" + contestId);
		}

		return () -> boardRepository.getBoard(contestId).get();
	}

	public void updateBoard(UUID contestId, IKumiteBoard currentBoard) {
		boardRepository.updateBoard(contestId, currentBoard);
	}
}
