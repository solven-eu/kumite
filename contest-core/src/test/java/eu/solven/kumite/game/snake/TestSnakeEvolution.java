package eu.solven.kumite.game.snake;

import java.util.ArrayList;
import java.util.List;
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
	public void testNoPlayer() {
		// The snake is on second row and second column
		SnakeBoard board = Snake.prepareNewBoard(snakeEvol.pos(1, 1), D3_).build();

		for (int i = 0; i < 16; i++) {
			board = snakeEvol.forwardSnake(randomGenerator, board, 1);
			Assertions.assertThat(board.getLife()).isEqualTo(0);
		}
	}

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
	public void testMoveForward() {
		SnakeBoard board = snake.generateInitialBoard(randomGenerator);
		board.setPlayerId(somePlayerId);

		Assertions.assertThat(board.getPositions()).hasSize(W * W);

		int nbFrame = 0;

		// After N frame, we shall hit a wall
		while (board.getLife() >= 0) {
			board = snakeEvol.forwardSnake(randomGenerator, board, 1);
			nbFrame++;
		}

		Assertions.assertThat(nbFrame).isEqualTo(7);
	}

	@Test
	public void testMoveOnce() {
		SnakeBoard initial = snake.generateInitialBoard(randomGenerator);
		initial.setPlayerId(somePlayerId);

		char initialHeadSymbol = initial.getPositions()[initial.getHeadPosition()];

		SnakeBoard moveOne = snakeEvol.forwardSnake(randomGenerator, initial, 1);

		Assertions.assertThat(moveOne.getPositions()[moveOne.getHeadPosition()]).isEqualTo(initialHeadSymbol);
		// The tail has turned into an empty cell
		Assertions.assertThat(moveOne.getPositions()[initial.getHeadPosition()]).isEqualTo('_');
	}

	@Test
	public void testMoveOnce_toFruit() {
		SnakeBoard initial = snake.generateInitialBoard(randomGenerator);
		initial.setPlayerId(somePlayerId);

		char initialHeadSymbol = initial.getPositions()[initial.getHeadPosition()];

		{
			SnakeBoard moveOne = snakeEvol.forwardSnake(randomGenerator, initial, 1);
			// Write a fruit in the position of the next head: if we replay the board, the snake shall eat the fruit
			initial.getPositions()[moveOne.getHeadPosition()] = F;
		}
		SnakeBoard moveOne = snakeEvol.forwardSnake(randomGenerator, initial, 1);

		Assertions.assertThat(moveOne.getPositions()[moveOne.getHeadPosition()]).isEqualTo(initialHeadSymbol);
		// The tail has turned into an empty cell
		Assertions.assertThat(moveOne.getPositions()[initial.getHeadPosition()]).isEqualTo(initialHeadSymbol);

		// Move again: the tail shall follow us
		{
			SnakeBoard moveTwo = snakeEvol.forwardSnake(randomGenerator, moveOne, 1);
			Assertions.assertThat(moveTwo.getPositions()[moveTwo.getHeadPosition()]).isEqualTo(initialHeadSymbol);
			Assertions.assertThat(moveTwo.getPositions()[moveOne.getHeadPosition()]).isEqualTo(initialHeadSymbol);
			Assertions.assertThat(moveTwo.getPositions()[initial.getHeadPosition()]).isEqualTo('_');
		}
	}

	// Snake goes left, then down, then right, then up, eating fruits on each step.
	@Test
	public void testSnakeSize4LoopOnItSelf() {
		SnakeBoard initial = Snake.prepareNewBoard(snakeEvol.pos(1, 1), D3_).build();
		initial.setPlayerId(somePlayerId);

		initial.getPositions()[snakeEvol.pos(2, 1)] = F;
		initial.getPositions()[snakeEvol.pos(2, 2)] = F;
		initial.getPositions()[snakeEvol.pos(1, 2)] = F;

		initial.setHeadDirection(D3_);
		SnakeBoard move2 = snakeEvol.forwardSnake(randomGenerator, initial, 1);
		Assertions.assertThat(move2.getFruit()).isEqualTo(1);
		Assertions.assertThat(move2.getHeadPosition()).isEqualTo(snakeEvol.pos(2, 1));
		Assertions.assertThat(move2.getTailPosition()).isEqualTo(snakeEvol.pos(1, 1));

		move2.setHeadDirection(D6_);
		SnakeBoard move3 = snakeEvol.forwardSnake(randomGenerator, move2, 1);
		Assertions.assertThat(move3.getFruit()).isEqualTo(2);
		Assertions.assertThat(move3.getHeadPosition()).isEqualTo(snakeEvol.pos(2, 2));
		Assertions.assertThat(move3.getTailPosition()).isEqualTo(snakeEvol.pos(1, 1));

		move3.setHeadDirection(D9_);
		SnakeBoard move4 = snakeEvol.forwardSnake(randomGenerator, move3, 1);
		Assertions.assertThat(move4.getFruit()).isEqualTo(3);
		Assertions.assertThat(move4.getHeadPosition()).isEqualTo(snakeEvol.pos(1, 2));
		Assertions.assertThat(move4.getTailPosition()).isEqualTo(snakeEvol.pos(1, 1));

		// We move to the initial head position: the tail should move to the right
		move4.setHeadDirection(D0_);
		SnakeBoard move5 = snakeEvol.forwardSnake(randomGenerator, move4, 1);
		Assertions.assertThat(move5.getFruit()).isEqualTo(3);
		Assertions.assertThat(move5.getHeadPosition()).isEqualTo(snakeEvol.pos(1, 1));
		Assertions.assertThat(move5.getTailPosition()).isEqualTo(snakeEvol.pos(2, 1));

		Assertions.assertThat(move5.getLife()).isEqualTo(0);
	}

	@Test
	public void testSnakeSize9LoopOnItSelf_DieEatingItself() {
		SnakeBoard initial = Snake.prepareNewBoard(snakeEvol.pos(1, 1), D3_).build();
		initial.setPlayerId(somePlayerId);

		initial.getPositions()[snakeEvol.pos(2, 1)] = F;
		initial.getPositions()[snakeEvol.pos(3, 1)] = F;
		initial.getPositions()[snakeEvol.pos(3, 2)] = F;
		initial.getPositions()[snakeEvol.pos(3, 3)] = F;
		initial.getPositions()[snakeEvol.pos(2, 3)] = F;
		initial.getPositions()[snakeEvol.pos(2, 2)] = F;

		List<SnakeBoard> boards = new ArrayList<>();

		boards.add(initial);

		initial.setHeadDirection(D3_);
		boards.add(snakeEvol.forwardSnake(randomGenerator, boards.getLast(), 1));
		boards.add(snakeEvol.forwardSnake(randomGenerator, boards.getLast(), 1));

		initial.setHeadDirection(D6_);
		boards.add(snakeEvol.forwardSnake(randomGenerator, boards.getLast(), 1));
		boards.add(snakeEvol.forwardSnake(randomGenerator, boards.getLast(), 1));

		initial.setHeadDirection(D9_);
		boards.add(snakeEvol.forwardSnake(randomGenerator, boards.getLast(), 1));

		initial.setHeadDirection(D0_);
		boards.add(snakeEvol.forwardSnake(randomGenerator, boards.getLast(), 1));

		Assertions.assertThat(boards.getLast().getLife()).isEqualTo(0);
		boards.add(snakeEvol.forwardSnake(randomGenerator, boards.getLast(), 1));
		Assertions.assertThat(boards.getLast().getLife()).isEqualTo(-1);

	}
}
