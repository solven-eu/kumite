package eu.solven.kumite.player;

import java.util.UUID;
import java.util.stream.Collectors;

import eu.solven.kumite.board.BoardsRegistry;
import eu.solven.kumite.board.IHasBoard;
import eu.solven.kumite.board.IKumiteBoard;
import eu.solven.kumite.board.persistence.IBoardRepository;
import lombok.AllArgsConstructor;

/**
 * This {@link IContendersRepository} fallback on {@link IBoardRepository} to list contenders.
 * 
 * @author Benoit Lacelle
 *
 */
@AllArgsConstructor
public class ContendersFromBoard implements IContendersRepository {
	final IAccountPlayersRegistry accountPlayersRegistry;
	final BoardsRegistry boardsRegistry;
	// final EventBus eventBus;

	private IKumiteBoard requireBoard(UUID contestId) {
		return boardsRegistry.makeDynamicBoardHolder(contestId).get();
	}

	@Override
	public boolean registerContender(UUID contestId, UUID playerId) {
		IKumiteBoard board = requireBoard(contestId);

		board.registerContender(playerId);

		// Persist the board (e.g. for concurrent changes)
		boardsRegistry.updateBoard(contestId, board);

		return true;
	}

	@Override
	public boolean isContender(UUID contestId, UUID playerId) {
		return requireBoard(contestId).snapshotPlayers().stream().anyMatch(p -> p.equals(playerId));
	}

	@Override
	public IHasPlayers makeDynamicHasPlayers(UUID contestId) {
		IHasBoard hasBoard = boardsRegistry.makeDynamicBoardHolder(contestId);

		return () -> hasBoard.get().snapshotPlayers().stream().map(playerId -> {
			UUID accountId = accountPlayersRegistry.getAccountId(playerId);
			return KumitePlayer.builder().playerId(playerId).accountId(accountId).build();
		}).collect(Collectors.toList());
	}

	@Override
	public void gameover(UUID contestId) {
		IKumiteBoard board = requireBoard(contestId);
		// if (board.isPresent()) {
		// TODO Checkif the board is actually over
		// board.get().
		// }
	}

	@Override
	public long getContestIds(UUID playerId) {
		return boardsRegistry.getContestIds(playerId);
	}

}
