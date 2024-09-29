package eu.solven.kumite.board;

import java.util.Optional;
import java.util.UUID;

import org.greenrobot.eventbus.EventBus;

import eu.solven.kumite.board.persistence.IBoardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class BoardsRegistry {
	final IBoardRepository boardRepository;
	final EventBus eventBus;

	public void registerBoard(UUID contestId, IKumiteBoard initialBoard) {
		Optional<IKumiteBoard> alreadyIn = boardRepository.putIfAbsent(contestId, initialBoard);
		if (alreadyIn.isPresent()) {
			throw new IllegalArgumentException(
					"board already registered (" + alreadyIn + ") for contestId=" + contestId);
		}
	}

	public IHasBoard makeDynamicBoardHolder(UUID contestId) {
		if (!boardRepository.hasContest(contestId)) {
			throw new IllegalArgumentException("Unknown contestId=" + contestId);
		}

		return () -> boardRepository.getBoard(contestId)
				.orElseThrow(() -> new IllegalStateException("The board has been removed in the meantime"));
	}

	public void updateBoard(UUID contestId, IKumiteBoard currentBoard) {
		boardRepository.updateBoard(contestId, currentBoard);

		// eventBus.post(BoardIsUpdated.builder().contestId(contestId).build());
	}

	public void forceGameover(UUID contestId) {
		// currentBoard.
		// TODO Auto-generated method stub

	}

	public long getContestIds(UUID playerId) {
		// TODO: Scan through live boards?
		return -1;
	}
}
