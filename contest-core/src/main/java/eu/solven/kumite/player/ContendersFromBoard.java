package eu.solven.kumite.player;

import java.util.UUID;
import java.util.stream.Collectors;

import eu.solven.kumite.board.BoardsRegistry;
import eu.solven.kumite.board.IHasBoard;
import eu.solven.kumite.board.IKumiteBoard;
import eu.solven.kumite.board.persistence.IBoardRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * This {@link IContendersRepository} fallback on {@link IBoardRepository} to list contenders.
 * 
 * @author Benoit Lacelle
 *
 */
@AllArgsConstructor
@Slf4j
public class ContendersFromBoard implements IContendersRepository {
	final IAccountPlayersRegistry accountPlayersRegistry;
	final BoardsRegistry boardsRegistry;

	private IKumiteBoard requireBoard(UUID contestId) {
		return boardsRegistry.hasBoard(contestId).get();
	}

	// This has to be called from within boardEvolutionThread
	@Override
	public UUID registerContender(UUID contestId, UUID playerId) {
		IKumiteBoard board = requireBoard(contestId);

		if (boardHasContender(playerId, board)) {
			throw new IllegalArgumentException(
					"playerId=%s is already a contender of contestId=%s".formatted(playerId, contestId));
		}

		board.registerContender(playerId);

		if (!boardHasContender(playerId, board)) {
			throw new IllegalStateException(
					"playerId=%s has not been registered as contender of contestId=%s".formatted(playerId, contestId));
		}

		// Persist the board (e.g. for concurrent changes)
		return boardsRegistry.updateBoard(contestId, board);
	}

	private boolean boardHasContender(UUID playerId, IKumiteBoard board) {
		return board.snapshotContenders().contains(playerId);
	}

	@Override
	public boolean isContender(UUID contestId, UUID playerId) {
		return requireBoard(contestId).snapshotContenders().stream().anyMatch(p -> p.equals(playerId));
	}

	@Override
	public IHasPlayers hasPlayers(UUID contestId) {
		IHasBoard hasBoard = boardsRegistry.hasBoard(contestId);

		return () -> hasBoard.get().snapshotContenders().stream().map(playerId -> {
			UUID accountId = accountPlayersRegistry.getAccountId(playerId);
			return KumitePlayer.builder().playerId(playerId).accountId(accountId).build();
		}).collect(Collectors.toList());
	}

	@Override
	public void gameover(UUID contestId) {
		// IContendersRepository is not responsible of propagating gameOver to BoardsRegistry
		log.debug("Nothing to do");
	}

	@Override
	public long getContestIds(UUID playerId) {
		return boardsRegistry.getContestIds(playerId);
	}

}
