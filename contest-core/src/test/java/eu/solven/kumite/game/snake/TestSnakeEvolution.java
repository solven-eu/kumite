package eu.solven.kumite.game.snake;

import java.util.Map;
import java.util.Random;
import java.util.random.RandomGenerator;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import eu.solven.kumite.account.login.IKumiteTestConstants;
import eu.solven.kumite.move.IKumiteMove;

public class TestSnakeEvolution implements ISnakeConstants, IKumiteTestConstants {
	Snake snake = new Snake();
	SnakeEvolution snakeEvol = new SnakeEvolution();
	RandomGenerator randomGenerator = new Random(0);

	@Test
	public void testCanMoveFromMiddle() {
		// The snake is on second row and second column
		SnakeBoard board = SnakeBoard.builder()
				.headPosition(snakeEvol.pos(1, 1))
				.tailPosition(snakeEvol.pos(1, 1))
				.playerId(somePlayerId)
				.build();

		Map<String, IKumiteMove> moves = snake.exampleMoves(randomGenerator, board, somePlayerId);

		Assertions.assertThat(moves).hasSize(4).containsKeys("0 - Up", "3 - Right", "6 - Down", "9 - Left");
	}

	@Test
	public void testCanMoveFromTopLeft() {
		// The snake is on second row and second column
		SnakeBoard board = SnakeBoard.builder()
				.headPosition(snakeEvol.pos(0, 0))
				.tailPosition(snakeEvol.pos(0, 0))
				.playerId(somePlayerId)
				.build();

		Map<String, IKumiteMove> moves = snake.exampleMoves(randomGenerator, board, somePlayerId);

		Assertions.assertThat(moves).hasSize(2).containsKeys("3 - Right", "6 - Down");
	}

	@Test
	public void testCanMoveFromTopRight() {
		// The snake is on second row and second column
		SnakeBoard board = SnakeBoard.builder().headPosition(W - 1).tailPosition(0).playerId(somePlayerId).build();

		Map<String, IKumiteMove> moves = snake.exampleMoves(randomGenerator, board, somePlayerId);

		Assertions.assertThat(moves).hasSize(2).containsKeys("6 - Down", "9 - Left");
	}

	@Test
	public void testInitial() {
		SnakeBoard board = snake.generateInitialBoard(randomGenerator);
		
		SnakeBoard evolved = snakeEvol.forwardSnake(board, 1);
	}
}
