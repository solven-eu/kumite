package eu.solven.kumite.game.snake;

import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.random.RandomGenerator;

import eu.solven.kumite.board.IKumiteBoard;
import eu.solven.kumite.board.IKumiteBoardView;
import eu.solven.kumite.board.realtime.IRealtimeGame;
import eu.solven.kumite.game.GameMetadata;
import eu.solven.kumite.game.IGame;
import eu.solven.kumite.game.IGameMetadataConstants;
import eu.solven.kumite.game.optimization.lag.Lag;
import eu.solven.kumite.game.snake.SnakeBoard.SnakeBoardBuilder;
import eu.solven.kumite.leaderboard.Leaderboard;
import eu.solven.kumite.move.IKumiteMove;
import eu.solven.kumite.move.PlayerMoveRaw;
import lombok.extern.slf4j.Slf4j;

/**
 * This snake is the simplest real-time game with a lose condition. it is especially useful to test real capabilities,
 * as it can be played can a single random player.
 * 
 * See {@link Lag} for a realtime game without losing condition.
 * 
 * @author Benoit Lacelle
 *
 */
@Slf4j
public class Snake implements IGame, IRealtimeGame, ISnakeConstants {
	GameMetadata gameMetadata = GameMetadata.builder()
			.gameId(UUID.fromString("aef8a04b-eda5-45d9-a936-1b5599c829e5"))
			.title("Snake")
			.tag(IGameMetadataConstants.TAG_SOLO)
			.tag(IGameMetadataConstants.TAG_REALTIME)
			.pace(Duration.ofMillis(100))
			.minPlayers(1)
			.maxPlayers(1)
			.shortDescription(
					"The player must keep the snake from colliding with both other obstacles and itself, which gets harder as the snake lengthens.")
			.reference(URI.create("https://en.wikipedia.org/wiki/Snake_(video_game_genre)"))
			.build();

	@Override
	public GameMetadata getGameMetadata() {
		return gameMetadata;
	}

	public static SnakeBoardBuilder prepareNewBoard(RandomGenerator random) {
		int headPosition = random.nextInt(W * W);
		int headDirection = random.nextInt(4) * 3;

		return prepareNewBoard(headPosition, headDirection);
	}

	public static SnakeBoardBuilder prepareNewBoard(int headPosition, int headDirection) {
		char[] positions = SnakeBoard.makeEmptyPositions();
		positions[headPosition] = SnakeEvolution.asChar(headDirection);

		// TODO We should pick a direction which is guaranteed not to lose right away
		return SnakeBoard.builder()
				.headPosition(headPosition)
				.headDirection(headDirection)
				.tailPosition(headPosition)
				.positions(positions);
	}

	@Override
	public SnakeBoard generateInitialBoard(RandomGenerator random) {
		return prepareNewBoard(random).life(1).build();
	}

	@Override
	public IKumiteBoard parseRawBoard(Map<String, ?> rawBoard) {
		return SnakeBoard.builder().build();
	}

	@Override
	public List<String> invalidMoveReasons(IKumiteBoardView rawBoard, PlayerMoveRaw playerMove) {
		SnakeBoard board = (SnakeBoard) rawBoard;
		SnakeMove move = (SnakeMove) playerMove.getMove();
		int direction = move.getDirection();

		if (direction != D0_ && direction != D3_ && direction != D6_ && direction != D9_) {
			return Collections.singletonList("direction=" + direction + " is not invalid");
		}

		int headPosition = board.getHeadPosition();
		boolean willHotTheWall = willHitTheWill(headPosition, direction);

		if (willHotTheWall) {
			return Collections.singletonList("direction=" + direction + " will hit the wall");

		}

		return Collections.emptyList();
	}

	public static boolean willHitTheWill(int headPosition, int direction) {
		boolean willHotTheWall;
		if (direction == D0_ && getRow(headPosition) == 0) {
			willHotTheWall = true;
		} else if (direction == D6_ && getRow(headPosition) == W - 1) {
			willHotTheWall = true;
		} else if (direction == D3_ && getColumn(headPosition) == W - 1) {
			willHotTheWall = true;
		} else if (direction == D9_ && getColumn(headPosition) == 0) {
			willHotTheWall = true;
		} else {
			willHotTheWall = false;
		}
		return willHotTheWall;
	}

	private static int getRow(int position) {
		return position / ISnakeConstants.W;
	}

	private static int getColumn(int position) {
		return position % ISnakeConstants.W;
	}

	@Override
	public IKumiteMove parseRawMove(Map<String, ?> rawMove) {
		Object rawDirection = rawMove.get("direction");
		if (rawDirection instanceof Number position) {
			int directionAsInt = position.intValue();

			if (directionAsInt != 0 && directionAsInt != 3 && directionAsInt != 6 && directionAsInt != 9) {
				throw new IllegalArgumentException("Invalid position: " + directionAsInt);
			}

			return SnakeMove.builder().direction(directionAsInt).build();
		}

		throw new IllegalArgumentException("Invalid position: " + rawMove);
	}

	@Override
	public Map<String, IKumiteMove> exampleMoves(RandomGenerator randomGenerator,
			IKumiteBoardView boardView,
			UUID playerId) {
		Map<String, IKumiteMove> moves = new TreeMap<>();

		// All possibles moves
		moves.put("0 - Up", SnakeMove.builder().direction(0).build());
		moves.put("3 - Right", SnakeMove.builder().direction(3).build());
		moves.put("6 - Down", SnakeMove.builder().direction(6).build());
		moves.put("9 - Left", SnakeMove.builder().direction(9).build());

		moves.keySet()
				.removeIf(move -> !invalidMoveReasons(boardView,
						PlayerMoveRaw.builder().playerId(playerId).move(moves.get(move)).build()).isEmpty());

		return moves;
	}

	@Override
	public boolean isGameover(IKumiteBoard rawBoard) {
		return ((SnakeBoard) rawBoard).getLife() < 0;
	}

	@Override
	public Leaderboard makeLeaderboard(IKumiteBoard rawBoard) {
		SnakeBoard board = (SnakeBoard) rawBoard;

		return board.makeLeaderboard();
	}

	@Override
	public IRealtimeGame getRealtimeGame() {
		return this;
	}

	@Override
	public Duration getPace() {
		return getGameMetadata().getPace();
	}

	@Override
	public IKumiteBoard forward(RandomGenerator randomGenerator, IKumiteBoard board, int nbFrameForward) {
		if (nbFrameForward != 1) {
			// The game will just go slower for the user. Snake with torus/unbounded world may accept unlimited
			// frame-forward
			log.debug("We stick to 1 frame (instead of {}), else the snake would quickly hit a wall", nbFrameForward);
		}
		return getEvolution().forwardSnake(randomGenerator, (SnakeBoard) board, 1);
	}

	private SnakeEvolution getEvolution() {
		return new SnakeEvolution();
	}

}
