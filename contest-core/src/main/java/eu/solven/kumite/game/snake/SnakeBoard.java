package eu.solven.kumite.game.snake;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import eu.solven.kumite.board.IKumiteBoard;
import eu.solven.kumite.board.IKumiteBoardView;
import eu.solven.kumite.leaderboard.Leaderboard;
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

	// Grid is 8x8
	@Builder.Default
	char[] positions = EMPTY.toCharArray();

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

	@Override
	public void registerMove(PlayerMoveRaw playerMove) {
		SnakeMove move = (SnakeMove) playerMove.getMove();

		headDirection = move.getDirection();
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

	// @Override
	// public String getBoardSvg() {
	// return "KumiteTicTacToeBoardState";
	// }
	//
	// @Override
	// public String getMoveSvg() {
	// return "KumiteTicTacToeBoardMove";
	// }

	@Override
	public List<UUID> snapshotContenders() {
		return Optional.ofNullable(playerId).stream().collect(Collectors.toList());
	}

	public Leaderboard makeLeaderboard() {
		// Should return the length of the snake as score
		return Leaderboard.empty();
	}
}
