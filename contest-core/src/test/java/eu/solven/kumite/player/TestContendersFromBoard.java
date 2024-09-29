package eu.solven.kumite.player;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import eu.solven.kumite.account.login.IKumiteTestConstants;
import eu.solven.kumite.board.BoardsRegistry;
import eu.solven.kumite.board.IHasBoard;
import eu.solven.kumite.board.IKumiteBoard;

public class TestContendersFromBoard implements IKumiteTestConstants {
	IAccountPlayersRegistry accountPlayerRegistry = Mockito.mock(IAccountPlayersRegistry.class);
	BoardsRegistry boardsRegistry = Mockito.mock(BoardsRegistry.class);

	ContendersFromBoard fromBoard = new ContendersFromBoard(accountPlayerRegistry, boardsRegistry);

	IHasBoard hasBoard = Mockito.mock(IHasBoard.class);
	IKumiteBoard board = Mockito.mock(IKumiteBoard.class);

	@Test
	public void testRegisterPlayer() {
		Mockito.when(hasBoard.get()).thenReturn(board);
		Mockito.when(boardsRegistry.makeDynamicBoardHolder(someContestId)).thenReturn(hasBoard);

		fromBoard.registerContender(someContestId, somePlayerId);

		// Ensure we persist the mutated board
		Mockito.verify(boardsRegistry).updateBoard(someContestId, board);
	}
}
