package eu.solven.kumite.player;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import eu.solven.kumite.account.login.IKumiteTestConstants;
import eu.solven.kumite.board.BoardsRegistry;
import eu.solven.kumite.board.IHasBoard;
import eu.solven.kumite.board.IKumiteBoard;
import eu.solven.kumite.board.persistence.InMemoryBoardMetadataRepository;
import eu.solven.kumite.board.persistence.InMemoryBoardRepository;
import eu.solven.kumite.game.optimization.lag.LagBoard;

public class TestContendersFromBoard implements IKumiteTestConstants {
	IAccountPlayersRegistry accountPlayerRegistry = Mockito.mock(IAccountPlayersRegistry.class);
	BoardsRegistry boardsRegistry =
			new BoardsRegistry(new InMemoryBoardRepository(), new InMemoryBoardMetadataRepository());

	ContendersFromBoard fromBoard = new ContendersFromBoard(accountPlayerRegistry, boardsRegistry);

	IHasBoard hasBoard = Mockito.mock(IHasBoard.class);
	IKumiteBoard board = Mockito.mock(IKumiteBoard.class);

	@Test
	public void testRegisterPlayer() {
		LagBoard boardBefore = LagBoard.builder().build();
		boardsRegistry.registerBoard(someContestId, boardBefore);

		fromBoard.registerContender(someContestId, somePlayerId);

		IKumiteBoard boardAfter = boardsRegistry.hasBoard(someContestId).get();

		// Ensure we persist the mutated board
		Assertions.assertThat(boardBefore.snapshotContenders()).isEmpty();
		Assertions.assertThat(boardAfter.snapshotContenders()).hasSize(1);
	}
}
