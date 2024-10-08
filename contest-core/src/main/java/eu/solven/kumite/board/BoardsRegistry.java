package eu.solven.kumite.board;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import eu.solven.kumite.board.persistence.IBoardMetadataRepository;
import eu.solven.kumite.board.persistence.IBoardRepository;
import eu.solven.kumite.contest.IHasGameover;
import eu.solven.kumite.exception.UnknownContestException;
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
	// If one need consistency over boardRepository and boardRepositoryMetadata, one should go through
	// BoardLifecycleManager. Many operations just need the board or the metadata, hence do not need
	// BoardLifecycleManager
	private static final ThreadLocal<Boolean> IS_BOARDEVOLUTION = ThreadLocal.withInitial(() -> null);

	final IBoardRepository boardRepository;
	final IBoardMetadataRepository boardMetadataRepository;

	// As registerBoard is synchronous on the contest initialization, no need to rely on BoardLifecycleManager
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

	public IHasBoard hasBoard(UUID contestId) {
		if (!boardRepository.hasContest(contestId)) {
			throw new UnknownContestException(contestId);
		}

		return () -> boardRepository.getBoard(contestId)
				.orElseThrow(() -> new IllegalStateException(
						"The board has been removed in the meantime contestId=" + contestId));
	}

	public IHasBoardMetadata getMetadata(UUID contestId) {
		if (!boardMetadataRepository.hasContest(contestId)) {
			throw new UnknownContestException(contestId);
		}

		return () -> boardMetadataRepository.getBoard(contestId)
				.orElseThrow(() -> new IllegalStateException(
						"The board has been removed in the meantime contestId=" + contestId));
	}

	public Supplier<IHasBoardAndMetadata> getBoardAndMetadata(UUID contestId) {
		IHasBoard hasBoard = hasBoard(contestId);
		IHasBoardMetadata hasMetadata = getMetadata(contestId);

		return () -> BoardAndMetadata.builder().board(hasBoard.get()).metadata(hasMetadata.get()).build();
	}

	public UUID updateBoard(UUID contestId, IKumiteBoard currentBoard) {
		checkBoardEvolutionThread();

		boardRepository.updateBoard(contestId, currentBoard);

		BoardDynamicMetadata metadata = getMetadata(contestId).get();
		BoardDynamicMetadata touched = metadata.touch();
		boardMetadataRepository.updateBoard(contestId, touched);

		return touched.getBoardStateId();
	}

	public static void checkBoardEvolutionThread() {
		if (IS_BOARDEVOLUTION.get() == null) {
			IS_BOARDEVOLUTION.set(true);
		} else if (!IS_BOARDEVOLUTION.get().booleanValue()) {
			throw new IllegalStateException("Can not updateBoard out of threadEvolutionThread");
		}
	}

	public UUID registerGameover(UUID contestId, boolean force) {
		// Read+Write: need to prevent interleaving operations
		checkBoardEvolutionThread();

		Optional<BoardDynamicMetadata> optBefore = boardMetadataRepository.getBoard(contestId);

		if (optBefore.isEmpty()) {
			throw new IllegalArgumentException("No contestId=%s".formatted(contestId));
		}

		BoardDynamicMetadata before = optBefore.get();
		if (before.getGameOverTs() == null) {
			BoardDynamicMetadata after = before.setGameOver();
			boardMetadataRepository.putIfPresent(contestId, after);
			log.info("gameOver force={} for contestId={}", force, contestId);
			return after.getBoardStateId();
		} else {
			log.info("Already gameOver for contestId={} (ts={})", contestId, before.getGameOverTs());
			return before.getBoardStateId();
		}
	}

	public UUID registerPlayerMoved(UUID contestId, UUID playerId) {
		// Read+Write: need to prevent interleaving operations
		checkBoardEvolutionThread();

		Optional<BoardDynamicMetadata> optBefore = boardMetadataRepository.getBoard(contestId);

		if (optBefore.isEmpty()) {
			throw new IllegalArgumentException("No contestId=%s".formatted(contestId));
		}

		BoardDynamicMetadata after = optBefore.get().playerMoved(playerId);
		boardMetadataRepository.putIfPresent(contestId, after);
		return after.getBoardStateId();
	}

	public IHasGameover hasGameover(IGame game, UUID contestId) {
		IHasBoard hasBoard = hasBoard(contestId);
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
