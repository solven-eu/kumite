package eu.solven.kumite.game.snake;

import java.util.random.RandomGenerator;

public class SnakeEvolution implements ISnakeConstants {

	/**
	 * Coordinates system follow the charIndex in a multi-line {@link String}: columns goes left from `0` to `W-1`
	 * 
	 * @param col
	 * @param row
	 * @return the index in a 2D-array, as if the board as represented by a multi-line {@link String}.
	 */
	public int pos(int col, int row) {
		if (col < 0 || col >= W) {
			throw new IllegalArgumentException("Invalid col=%s".formatted(col));
		} else if (row < 0 || row >= W) {
			throw new IllegalArgumentException("Invalid row=%s".formatted(row));
		}

		return col + row * W;
	}

	public SnakeBoard forwardSnake(RandomGenerator randomGenerator, SnakeBoard board, int nbFrame) {
		if (Snake.willHitTheWill(board.headPosition, board.headDirection)) {
			return Snake.prepareNewBoard(randomGenerator)
					.fruit(board.fruit)
					.life(board.life - 1)
					.playerId(board.playerId)
					.build();
		}

		int newHeadPosition = board.headPosition + nbFrame * directionToPositionShift(board.headDirection);

		char[] newPositions = board.positions.clone();

		// Update the previous head position. It is crucial to walk the snake backward
		// newPositions[board.headPositionIndex] = asString(board.headDirection).charAt(0);
		// Write the new head

		char beforeNewHead = newPositions[newHeadPosition];

		int fruit = board.fruit;

		int newTailPosition;
		if (beforeNewHead == F) {
			// We eat a fruit: the tail does not move forward
			newTailPosition = board.tailPosition;
			fruit++;
		} else {
			// The tail moves forward
			char tailDirectionChar = board.positions[board.tailPosition];
			int tailDirection = toDirection(tailDirectionChar);

			newTailPosition = board.tailPosition + nbFrame * directionToPositionShift(tailDirection);
			newPositions[board.tailPosition] = '_';
		}

		char newHead = newPositions[newHeadPosition];
		if (newHead == D0 || newHead == D3 || newHead == D6 || newHead == D9) {
			// The snake is eating itself
			return Snake.prepareNewBoard(randomGenerator)
					.fruit(board.fruit)
					.life(board.life - 1)
					.playerId(board.playerId)
					.build();
		}

		newPositions[newHeadPosition] = asChar(board.headDirection);

		// Generate a new fruit every 8 moves (statistically)
		if (0 == randomGenerator.nextInt(8)) {
			int nbFree = 0;
			for (char c : newPositions) {
				if (c == '_') {
					nbFree++;
				}
			}

			int positionToFruit = randomGenerator.nextInt(nbFree);

			int indexFree = 0;
			for (int i = 0; i < newPositions.length; i++) {
				char c = newPositions[i];
				if (c == '_') {

					if (indexFree == positionToFruit) {
						newPositions[indexFree] = 'F';
						break;
					}

					indexFree++;
				}
			}
		}

		return SnakeBoard.builder()
				.playerId(board.playerId)
				// Keep the direction
				.headDirection(board.headDirection)
				.headPosition(newHeadPosition)
				.tailPosition(newTailPosition)
				.positions(newPositions)
				.fruit(fruit)
				.build();
	}

	static int toDirection(char directionChar) {
		return switch (directionChar) {
		case D0:
			yield 0;
		case D3:
			yield 3;
		case D6:
			yield 6;
		case D9:
			yield 9;
		default:
			throw new IllegalArgumentException("Unexpected value: " + directionChar);
		};
	}

	private static int directionToPositionShift(int headDirection) {
		return switch (headDirection) {
		case 0:
			// Previous rows
			yield -W;
		case 3:
			// Next column
			yield 1;
		case 6:
			// next row
			yield W;
		case 9:
			// Previous column
			yield -1;
		default:
			throw new IllegalArgumentException("Unexpected value: " + headDirection);
		};
	}

	static char asChar(int direction) {
		return switch (direction) {
		case 0:
			yield D0;
		case 3:
			yield D3;
		case 6:
			yield D6;
		case 9:
			yield D9;

		default:
			throw new IllegalArgumentException("Unexpected value: " + direction);
		};
	}
}
