package eu.solven.kumite.player;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import eu.solven.kumite.account.login.IKumiteTestConstants;
import eu.solven.kumite.board.IKumiteBoard;
import eu.solven.kumite.board.persistence.IBoardRepository;

public class TestContendersFromBoard implements IKumiteTestConstants {
	IAccountPlayersRegistry accountPlayerRegistry = Mockito.mock(IAccountPlayersRegistry.class);
	IBoardRepository boardRepository = Mockito.mock(IBoardRepository.class);

	ContendersFromBoard fromBoard = new ContendersFromBoard(accountPlayerRegistry, boardRepository);

	IKumiteBoard board = Mockito.mock(IKumiteBoard.class);

	@Test
	public void testRegisterPlayer() {
		Mockito.when(boardRepository.getBoard(someContestId)).thenReturn(Optional.of(board));

		fromBoard.registerContender(someContestId, somePlayerId);

		// Ensure we persist the mutated board
		Mockito.verify(boardRepository).updateBoard(someContestId, board);
	}
}
