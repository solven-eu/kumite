package eu.solven.kumite.game.snake;

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

	public SnakeBoard forwardSnake(SnakeBoard board, int nbFrame) {
		int newHeadPosition = board.headPosition + nbFrame * directionToPositionShift(board.headDirection);

		char[] newPositions = board.positions.clone();

		// Update the previous head position. It is crucial to walk the snake backward
		// newPositions[board.headPositionIndex] = asString(board.headDirection).charAt(0);
		// Write the new head

		char beforeNewHead = newPositions[newHeadPosition];

		newPositions[newHeadPosition] = asChar(board.headDirection);

		int newTailPosition;
		if (beforeNewHead == F) {
			// We eat a fruit: the tail does not move forward
			newTailPosition = board.tailPosition;
		} else if (beforeNewHead == '_') {
			// The tail moves forward
			char tailDirectionChar = board.positions[board.tailPosition];
			int tailDirection = toDirection(tailDirectionChar);

			newTailPosition = board.tailPosition + nbFrame * directionToPositionShift(tailDirection);
		} else {
			// TODO The snake eat itself
			// TODO The snake hits a wall
			newTailPosition = 0;
		}

		return SnakeBoard.builder()
				.playerId(board.playerId)
				// Keep the direction
				.headDirection(board.headDirection)
				.headPosition(newHeadPosition)
				.tailPosition(newTailPosition)
				.positions(newPositions)
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
