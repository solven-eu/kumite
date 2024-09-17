package eu.solven.kumite.player;

import java.util.UUID;

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
public class ContestPlayersFromBoard implements IContendersRepository {
	final IBoardRepository boardRepository;

	private IKumiteBoard requireBoard(UUID contestId) {
		return boardRepository.getBoard(contestId)
				.orElseThrow(() -> new IllegalArgumentException("No board for contestId=" + contestId));
	}

	@Override
	public boolean registerContender(UUID contestId, UUID playerId) {
		IKumiteBoard optBoard = requireBoard(contestId);

//		if (optBoard.snapshotPlayers().stream().noneMatch(p -> p.getPlayerId().equals(playerId))) {
//			throw new IllegalStateException("The board should have registered the player");
//		}
		optBoard.registerContender(playerId);

		return true;
	}

	@Override
	public boolean isContender(UUID contestId, UUID playerId) {
		return requireBoard(contestId).snapshotPlayers().stream().anyMatch(p -> p.getPlayerId().equals(playerId));
	}

	@Override
	public IHasPlayers makeDynamicHasPlayers(UUID contestId) {
		if (!boardRepository.containsKey(contestId)) {
			throw new IllegalArgumentException("Unknown contestId=" + contestId);
		}
		return () -> requireBoard(contestId).snapshotPlayers();
	}

}
