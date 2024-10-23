package eu.solven.kumite.game.snake;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import eu.solven.kumite.board.IKumiteBoard;
import eu.solven.kumite.board.IKumiteBoardView;
import eu.solven.kumite.leaderboard.Leaderboard;
import eu.solven.kumite.leaderboard.PlayerLongScore;
import eu.solven.kumite.move.PlayerMoveRaw;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;

@Data
@Builder
@Jacksonized
@JsonIgnoreProperties(value = { "boardSvg", "moveSvg" }, allowGetters = true)
@Slf4j
public class SnakeBoard implements IKumiteBoard, IKumiteBoardView, ISnakeConstants {

	private static char[] toChars(int[] ints) {
		char[] chars = new char[ints.length];
		for (int i = 0; i < ints.length; i++) {
			chars[i] = (char) ints[i];
		}
		return chars;
	}

	public static char[] makeEmptyPositions() {
		return toChars(EMPTY.chars().filter(c -> c != '\r' && c != '\n').toArray());
	}

	// Grid is 8x8
	@Builder.Default
	char[] positions = makeEmptyPositions();

	@Default
	UUID playerId = null;
	@Default
	int headPosition = -1;
	@Default
	int tailPosition = -1;
	// The head direction: if changed by the player, it will be integrated in the board on next forward-step.
	@Default
	int headDirection = -1;

	// By default, we have no `life`: next gameOver triggers contest gameOver
	@Default
	int life = 0;

	@Default
	int fruit = 0;

	@Override
	public void registerMove(PlayerMoveRaw playerMove) {
		SnakeMove move = (SnakeMove) playerMove.getMove();

		headDirection = move.getDirection();
		positions[headPosition] = SnakeEvolution.asChar(headDirection);
	}

	@Override
	public IKumiteBoardView asView(UUID playerId) {
		// Every players can see the whole board
		return this;
	}

	@Override
	public void registerContender(UUID playerId) {
		if (this.playerId == null) {
			this.playerId = playerId;
		} else {
			throw new IllegalStateException("There is already a player in this solo game: " + playerId);
		}
	}

	@Override
	public String getBoardSvg() {
		return "KumiteSnakeBoardState";
	}

	@Override
	public String getMoveSvg() {
		return "KumiteSnakeBoardMove";
	}

	@Override
	public List<UUID> snapshotContenders() {
		return Optional.ofNullable(playerId).stream().collect(Collectors.toList());
	}

	public Leaderboard makeLeaderboard() {
		if (playerId == null) {
			return Leaderboard.empty();
		}
		PlayerLongScore score = PlayerLongScore.builder().playerId(playerId).score(fruit).build();
		return Leaderboard.builder().playerIdToPlayerScore(Map.of(playerId, score)).build();
	}

	@Override
	public String toString() {
		// -1 as we do not need a trailing `\n`
		char[] withEol = new char[W * W + W - 1];

		for (int i = 0; i < W; i++) {
			for (int j = 0; j < W; j++) {
				withEol[i * W + j + i] = positions[i * W + j];
			}
			if (i < W - 1) {
				withEol[i * W + W + i] = '\n';
			}
		}

		return new String(withEol);
	}
}
