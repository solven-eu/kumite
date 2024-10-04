package eu.solven.kumite.board;

import java.util.Optional;
import java.util.UUID;

import eu.solven.kumite.board.persistence.IBoardMetadataRepository;
import eu.solven.kumite.board.persistence.IBoardRepository;
import eu.solven.kumite.contest.IHasGameover;
import eu.solven.kumite.game.IGame;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Write operations should be done through {@link BoardLifecycleManager}. This will ensure thread-safety, and
 * consistency on concurrent player moves.
 * 
 * @author Benoit Lacelle
 *
 */
@Slf4j
@RequiredArgsConstructor
public class BoardsRegistry {
	final IBoardRepository boardRepository;
	final IBoardMetadataRepository boardMetadataRepository;

	public void registerBoard(UUID contestId, IKumiteBoard initialBoard) {
		{
			Optional<IKumiteBoard> alreadyIn = boardRepository.putIfAbsent(contestId, initialBoard);
			if (alreadyIn.isPresent()) {
				throw new IllegalArgumentException(
						"board already registered (" + alreadyIn + ") for contestId=" + contestId);
			}
		}
		{
			Optional<BoardDynamicMetadata> alreadyIn =
					boardMetadataRepository.putIfAbsent(contestId, BoardDynamicMetadata.builder().build());
			if (alreadyIn.isPresent()) {
				throw new IllegalArgumentException(
						"boardMetadata already registered (" + alreadyIn + ") for contestId=" + contestId);
			}
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
	}

	public void forceGameover(UUID contestId) {
		Optional<BoardDynamicMetadata> optBefore = boardMetadataRepository.getBoard(contestId);

		optBefore.ifPresent(before -> {
			BoardDynamicMetadata after = before.setGameOver();
			boardMetadataRepository.putIfPresent(contestId, after);
			log.info("Force gameOver for contestId={}", contestId);
		});

	}

	public void markGameover(UUID contestId) {
		Optional<BoardDynamicMetadata> optBefore = boardMetadataRepository.getBoard(contestId);

		optBefore.ifPresent(before -> {
			BoardDynamicMetadata after = before.setGameOver();
			boardMetadataRepository.putIfPresent(contestId, after);
			log.info("Mark gameOver for contestId={}", contestId);
		});

	}

	public IHasGameover hasGameover(IGame game, UUID contestId) {
		IHasBoard hasBoard = makeDynamicBoardHolder(contestId);
		return () -> {
			Optional<BoardDynamicMetadata> optMetadata = boardMetadataRepository.getBoard(contestId);
			boolean gameIsOver = optMetadata.map(m -> m.getGameOverTs() != null)
					// If no board, we consider the game is over
					.orElse(true);

			if (gameIsOver) {
				return true;
			}

			return game.isGameover(hasBoard.get());
		};
	}

	public long getContestIds(UUID playerId) {
		// TODO: Scan through live boards?
		return -1;
	}
}
