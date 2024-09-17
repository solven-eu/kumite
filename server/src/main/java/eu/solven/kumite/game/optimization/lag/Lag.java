package eu.solven.kumite.game.optimization.lag;

import java.net.URI;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.random.RandomGenerator;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.solven.kumite.board.IHasBoard;
import eu.solven.kumite.board.IKumiteBoard;
import eu.solven.kumite.board.IKumiteBoardView;
import eu.solven.kumite.contest.IHasGameover;
import eu.solven.kumite.game.GameMetadata;
import eu.solven.kumite.game.IGame;
import eu.solven.kumite.game.IGameMetadataConstants;
import eu.solven.kumite.leaderboard.IPlayerScore;
import eu.solven.kumite.leaderboard.LeaderBoard;
import eu.solven.kumite.leaderboard.PlayerLongScore;
import eu.solven.kumite.player.IKumiteMove;
import lombok.Value;

@Value
public class Lag implements IGame {
	GameMetadata gameMetadata = GameMetadata.builder()
			.gameId(UUID.fromString("b606f730-cce4-44c7-a41f-44d557bd517f"))
			.title("Lag")
			.tag(IGameMetadataConstants.TAG_OPTIMIZATION)
			.tag(IGameMetadataConstants.TAG_PERFECT_INFORMATION)
			.tag(IGameMetadataConstants.TAG_REALTIME)
			.maxPlayers(Integer.MAX_VALUE)
			.shortDescription("You score your Lag. This is the simplest realtime game")
			.reference(URI.create("https://en.wikipedia.org/wiki/Lag_(video_games)"))
			.build();

	@Override
	public boolean isValidMove(IKumiteMove move) {
		return true;
	}

	@Override
	public LagBoard generateInitialBoard(RandomGenerator random) {
		return LagBoard.builder().build();
	}

	@Override
	public LagServerTimestamp parseRawMove(Map<String, ?> rawMove) {
		return new ObjectMapper().convertValue(rawMove, LagServerTimestamp.class);
	}

	@Override
	public IKumiteBoard parseRawBoard(Map<String, ?> rawBoard) {
		return new ObjectMapper().convertValue(rawBoard, LagBoard.class);
	}

	@Override
	public LeaderBoard makeLeaderboard(IKumiteBoard board) {
		Map<UUID, IPlayerScore> playerToScore = new TreeMap<>();

		LagBoard tspBoard = (LagBoard) board;
		tspBoard.getPlayerToLatestLagMs().forEach((playerId, lag) -> {
			long score = lag;
			playerToScore.put(playerId, PlayerLongScore.builder().playerId(playerId).score(score).build());
		});

		return LeaderBoard.builder().playerIdToPlayerScore(playerToScore).build();
	}

	@Override
	public Map<String, IKumiteMove> exampleMoves(IKumiteBoardView boardView, UUID playerId) {
		return Map.of("now",
				LagServerTimestamp.builder().moveTimestamp(Long.toString(System.currentTimeMillis())).build());
	}

	@Override
	public IHasGameover makeDynamicGameover(IHasBoard rawBoard) {
		// TODO Implement a timeout logic
		return () -> false;
	}
}
