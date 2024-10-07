package eu.solven.kumite.board;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestBoardDynamicMetadata {
	@Test
	public void testGameOverTwice() throws InterruptedException {
		BoardDynamicMetadata board = BoardDynamicMetadata.builder().build();

		BoardDynamicMetadata boardGameover1 = board.setGameOver();
		OffsetDateTime gameOverTs = boardGameover1.getGameOverTs();

		TimeUnit.MILLISECONDS.sleep(10);

		BoardDynamicMetadata boardGameover2 = boardGameover1.setGameOver();

		Assertions.assertThat(boardGameover2).isEqualTo(boardGameover1);
		Assertions.assertThat(boardGameover2.getGameOverTs()).isEqualTo(gameOverTs);
	}

	@Test
	public void testPlayerPlayedTwice() throws InterruptedException {
		BoardDynamicMetadata board1 = BoardDynamicMetadata.builder().build();
		Assertions.assertThat(board1.getPlayerIdToLastMove()).isEmpty();

		UUID playerId = UUID.randomUUID();
		BoardDynamicMetadata board2 = board1.playerMoved(playerId);
		Assertions.assertThat(board2.getPlayerIdToLastMove()).containsKey(playerId).hasSize(1);
		OffsetDateTime initialPlayedTs = board2.getPlayerIdToLastMove().values().iterator().next();

		// Same player player later
		BoardDynamicMetadata board3;
		{
			TimeUnit.MILLISECONDS.sleep(10);
			board3 = board2.playerMoved(playerId);
			Assertions.assertThat(board3.getPlayerIdToLastMove()).containsKey(playerId).hasSize(1);
			OffsetDateTime laterPlayedTs = board3.getPlayerIdToLastMove().values().iterator().next();

			Assertions.assertThat(laterPlayedTs).isAfter(initialPlayedTs);
		}

		// Another player played
		UUID playerId2 = UUID.randomUUID();
		BoardDynamicMetadata board4;
		{
			TimeUnit.MILLISECONDS.sleep(10);
			board4 = board3.playerMoved(playerId2);
			Assertions.assertThat(board4.getPlayerIdToLastMove()).containsKeys(playerId, playerId2).hasSize(2);
			OffsetDateTime laterPlayedTs = board4.getPlayerIdToLastMove().values().iterator().next();

			Assertions.assertThat(laterPlayedTs).isAfter(initialPlayedTs);
		}
	}
}
