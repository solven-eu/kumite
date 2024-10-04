package eu.solven.kumite.board;

import java.time.OffsetDateTime;
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
}
