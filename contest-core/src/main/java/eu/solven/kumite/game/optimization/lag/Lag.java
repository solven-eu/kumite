package eu.solven.kumite.game.optimization.lag;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.random.RandomGenerator;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.solven.kumite.board.IKumiteBoard;
import eu.solven.kumite.board.IKumiteBoardView;
import eu.solven.kumite.game.GameMetadata;
import eu.solven.kumite.game.IGame;
import eu.solven.kumite.game.IGameMetadataConstants;
import eu.solven.kumite.game.snake.Snake;
import eu.solven.kumite.leaderboard.IPlayerScore;
import eu.solven.kumite.leaderboard.Leaderboard;
import eu.solven.kumite.leaderboard.PlayerLongScore;
import eu.solven.kumite.move.IKumiteMove;
import eu.solven.kumite.move.PlayerMoveRaw;
import lombok.Value;

/**
 * {@link Lag} is the simplest real-time game. It has no losing condition. See {@link Snake} for a simple realtime game
 * with a losing condition.
 * 
 * @author Benoit Lacelle
 *
 */
@Value
public class Lag implements IGame {
	GameMetadata gameMetadata = GameMetadata.builder()
			.gameId(UUID.fromString("b606f730-cce4-44c7-a41f-44d557bd517f"))
			.title("Lag")
			.tag(IGameMetadataConstants.TAG_OPTIMIZATION)
			.tag(IGameMetadataConstants.TAG_PERFECT_INFORMATION)
			.tag(IGameMetadataConstants.TAG_REALTIME)
			.maxPlayers(1024)
			.shortDescription("You score your Lag. This is the simplest realtime game")
			.reference(URI.create("https://en.wikipedia.org/wiki/Lag_(video_games)"))
			.build();

	@Override
	public LagBoard generateInitialBoard(RandomGenerator random) {
		return LagBoard.builder().build();
	}

	@Override
	public IKumiteBoard parseRawBoard(Map<String, ?> rawBoard) {
		return new ObjectMapper().convertValue(rawBoard, LagBoard.class);
	}

	@Override
	public List<String> invalidMoveReasons(IKumiteBoardView rawBoardView, PlayerMoveRaw playerMove) {
		return Collections.emptyList();
	}

	@Override
	public LagServerTimestamp parseRawMove(Map<String, ?> rawMove) {
		return new ObjectMapper().convertValue(rawMove, LagServerTimestamp.class);
	}

	@Override
	public Leaderboard makeLeaderboard(IKumiteBoard board) {
		Map<UUID, IPlayerScore> playerToScore = new TreeMap<>();

		LagBoard tspBoard = (LagBoard) board;
		tspBoard.getPlayerToLatestLagMs().forEach((playerId, lag) -> {
			long score = lag;
			playerToScore.put(playerId, PlayerLongScore.builder().playerId(playerId).score(score).build());
		});

		return Leaderboard.builder().playerIdToPlayerScore(playerToScore).build();
	}

	@Override
	public Map<String, IKumiteMove> exampleMoves(RandomGenerator randomGenerator,
			IKumiteBoardView boardView,
			UUID playerId) {
		return Map.of("now",
				LagServerTimestamp.builder().moveTimestamp(Long.toString(System.currentTimeMillis())).build());
	}

	@Override
	public boolean isGameover(IKumiteBoard board) {
		return false;
	}

}
