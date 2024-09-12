package eu.solven.kumite.game.opposition.tictactoe;

import java.nio.CharBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.TreeMap;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import eu.solven.kumite.board.IKumiteBoard;
import eu.solven.kumite.board.IKumiteBoardView;
import eu.solven.kumite.player.PlayerMoveRaw;
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
	public List<String> isValidMove(PlayerMoveRaw playerMove) {
		char nextPlayerSymbol = getNextPlayerSymbol();

		char playerMoveSymbol = getPlayerSymbol(playerMove.getPlayerId());
		if (Character.compare(nextPlayerSymbol, playerMoveSymbol) != 0) {
			return Collections.singletonList("playerId=" + playerMove.getPlayerId() + " can not play for now");
		}

		TicTacToeMove move = (TicTacToeMove) playerMove.getMove();
		int position = move.getPosition();

		char positionCurrentSymbol = positions[position - 1];
		if (positionCurrentSymbol != '_') {
			return Collections.singletonList("position=" + position + " is not empty (" + positionCurrentSymbol + ")");
		}

		return Collections.emptyList();
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
	public void registerPlayer(UUID playerId) {
		if (playerIdToSymbol.isEmpty()) {
			playerIdToSymbol.put(playerId, 'X');
		} else if (playerIdToSymbol.size() == 1) {
			playerIdToSymbol.put(playerId, 'O');
		} else {
			throw new IllegalArgumentException("There is already 2 players");
		}

	}

	public OptionalInt optWinningChar() {
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
		if (!hasLeftMove()) {
			// Not a single playable position: the game is over (may be a draw)
			return true;
		}

		OptionalInt winningChar = optWinningChar();

		// As soon as there is a winnignChar, the game is over
		return winningChar.isPresent();
	}

	/**
	 * 
	 * @return true if there is at least one playable position
	 */
	private boolean hasLeftMove() {
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
}
