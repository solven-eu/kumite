package eu.solven.kumite.player;

import java.util.UUID;
import java.util.stream.Collectors;

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
	final IBoardRepository boardRepository;

	private IKumiteBoard requireBoard(UUID contestId) {
		return boardRepository.getBoard(contestId)
				.orElseThrow(() -> new IllegalArgumentException("No board for contestId=" + contestId));
	}

	@Override
	public boolean registerContender(UUID contestId, UUID playerId) {
		IKumiteBoard board = requireBoard(contestId);

		board.registerContender(playerId);

		// Persist the board (e.g. for concurrent changes)
		boardRepository.updateBoard(contestId, board);

		return true;
	}

	@Override
	public boolean isContender(UUID contestId, UUID playerId) {
		return requireBoard(contestId).snapshotPlayers().stream().anyMatch(p -> p.equals(playerId));
	}

	@Override
	public IHasPlayers makeDynamicHasPlayers(UUID contestId) {
		if (!boardRepository.hasContest(contestId)) {
			throw new IllegalArgumentException("Unknown contestId=" + contestId);
		}
		return () -> requireBoard(contestId).snapshotPlayers().stream().map(playerId -> {
			UUID accountId = accountPlayersRegistry.getAccountId(playerId);
			return KumitePlayer.builder().playerId(playerId).accountId(accountId).build();
		}).collect(Collectors.toList());
	}

}
