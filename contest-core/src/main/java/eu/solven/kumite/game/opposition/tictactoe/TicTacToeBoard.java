package eu.solven.kumite.game.opposition.tictactoe;

import java.nio.CharBuffer;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import eu.solven.kumite.board.IKumiteBoard;
import eu.solven.kumite.board.IKumiteBoardView;
import eu.solven.kumite.leaderboard.Leaderboard;
import eu.solven.kumite.leaderboard.PlayerLongScore;
import eu.solven.kumite.move.PlayerMoveRaw;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
@JsonIgnoreProperties(value = { "boardSvg", "moveSvg" }, allowGetters = true)
public class TicTacToeBoard implements IKumiteBoard, IKumiteBoardView {
	// The first player is designated as X. The second player is designated as O.
	// @JsonSerialize(using = StdArraySerializers.CharArraySerializer.class)
	@Builder.Default
	char[] positions = { '_', '_', '_', '_', '_', '_', '_', '_', '_' };

	@Builder.Default
	Map<UUID, Character> playerIdToSymbol = new TreeMap<>();

	char getNextPlayerSymbol() {
		long countX = CharBuffer.wrap(positions).chars().filter(c -> c == 'X').count();
		long countO = CharBuffer.wrap(positions).chars().filter(c -> c == 'O').count();

		char nextPlayerSymbol;
		if (countX == countO) {
			// Next turn is X
			nextPlayerSymbol = 'X';
		} else {
			// Next turn is O
			nextPlayerSymbol = 'O';
		}
		return nextPlayerSymbol;
	}

	char getPlayerSymbol(UUID playerId) {
		Character symbol = playerIdToSymbol.get(playerId);

		if (symbol == null) {
			throw new IllegalArgumentException(
					"playerId=" + playerId + " is not registered. playerIdToSymbol=" + playerIdToSymbol);
		}

		return symbol;
	}

	@Override
	public void registerMove(PlayerMoveRaw playerMove) {
		if (playerIdToSymbol.size() != 2) {
			throw new IllegalArgumentException("We need 2 players before accepting moves");
		}

		TicTacToeMove move = (TicTacToeMove) playerMove.getMove();
		int position = move.getPosition();

		char nextPlayerSymbol = getNextPlayerSymbol();
		positions[position - 1] = nextPlayerSymbol;
	}

	@Override
	public IKumiteBoardView asView(UUID playerId) {
		// Every players can see the whole board
		return this;
	}

	@Override
	public void registerContender(UUID playerId) {
		if (playerIdToSymbol.containsKey(playerId)) {
			throw new IllegalArgumentException("playerId=" + playerId + " is already registered");
		} else if (playerIdToSymbol.isEmpty()) {
			playerIdToSymbol.put(playerId, 'X');
		} else if (playerIdToSymbol.size() == 1) {
			playerIdToSymbol.put(playerId, 'O');
		} else {
			throw new IllegalArgumentException("There is already 2 players: " + playerIdToSymbol);
		}
	}

	/**
	 * 
	 * @return if there is a winning position, the symbol of the winner
	 */
	protected OptionalInt optWinningSymbol() {
		for (int i = 0; i < 3; i++) {
			// Checking columns
			if (positions[0 + i] != '_' && positions[0 + i] == positions[3 + i]
					&& positions[3 + i] == positions[6 + i]) {
				return OptionalInt.of(positions[0 + i]);
			}

			// Checking rows
			if (positions[0 + 3 * i] != '_' && positions[0 + 3 * i] == positions[1 + 3 * i]
					&& positions[1 + 3 * i] == positions[2 + 3 * i]) {
				return OptionalInt.of(positions[0 + 3 * i]);
			}
		}

		// Checking diagonals
		if (positions[0] != '_' && positions[0] == positions[4] && positions[4] == positions[8]) {
			return OptionalInt.of(positions[0]);
		} else if (positions[2] != '_' && positions[2] == positions[4] && positions[4] == positions[6]) {
			return OptionalInt.of(positions[2]);
		}

		return OptionalInt.empty();
	}

	// Ignored as the gameOver should be computed by the game, not hold in the board itself
	@JsonIgnore
	public boolean isGameOver() {
		if (!hasAvailableMove()) {
			// Not a single playable position: the game is over (may be a draw)
			return true;
		}

		OptionalInt winningChar = optWinningSymbol();

		// As soon as there is a winnignChar, the game is over
		return winningChar.isPresent();
	}

	public Leaderboard makeLeaderboard() {
		Leaderboard leaderboard = Leaderboard.builder().build();

		if (!isGameOver()) {
			playerIdToSymbol.forEach((playerId, symbol) -> {
				leaderboard.registerScore(PlayerLongScore.builder().playerId(playerId).score(0).build());
			});

			return leaderboard;
		}

		OptionalInt optWinningSymbol = optWinningSymbol();

		if (optWinningSymbol.isPresent()) {
			int winningChar = optWinningSymbol.getAsInt();

			playerIdToSymbol.forEach((playerId, symbol) -> {
				if (symbol.charValue() == winningChar) {
					// 3 points for a win
					leaderboard.registerScore(PlayerLongScore.builder().playerId(playerId).score(3).build());
				} else {
					// 1 point for a lose
					leaderboard.registerScore(PlayerLongScore.builder().playerId(playerId).score(1).build());
				}
			});
		} else {
			playerIdToSymbol.forEach((playerId, symbol) -> {
				// 2 point for a tie
				leaderboard.registerScore(PlayerLongScore.builder().playerId(playerId).score(2).build());
			});
		}

		return leaderboard;
	}

	/**
	 * 
	 * @return true if there is at least one playable position
	 */
	private boolean hasAvailableMove() {
		for (char someChar : positions) {
			if (someChar == '_') {
				return true;
			}
		}

		return false;
	}

	@Override
	public String getBoardSvg() {
		return "KumiteTicTacToeBoardState";
	}

	@Override
	public String getMoveSvg() {
		return "KumiteTicTacToeBoardMove";
	}

	@Override
	public List<UUID> snapshotContenders() {
		return playerIdToSymbol.keySet().stream().collect(Collectors.toList());
	}
}
