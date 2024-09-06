package eu.solven.kumite.game.opposition.tictactoe;

import java.nio.CharBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import eu.solven.kumite.board.IKumiteBoard;
import eu.solven.kumite.game.optimization.tsp.IKumiteBoardView;
import eu.solven.kumite.player.PlayerMoveRaw;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
@JsonIgnoreProperties(value = "svg", allowGetters = true)
public class TicTacToeBoard implements IKumiteBoard, IKumiteBoardView {
	// The first player is designated as X. The second player is designated as O.
	// @JsonSerialize(using = StdArraySerializers.CharArraySerializer.class)
	@Builder.Default
	char[] positions = new char[] { ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' };

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
			throw new IllegalArgumentException("playerId=" + playerId + " is not registered");
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
		if (positionCurrentSymbol != ' ') {
			return Collections.singletonList("position=" + position + " is not empty (" + positionCurrentSymbol + ")");
		}

		return Collections.emptyList();
	}

	@Override
	public void registerMove(PlayerMoveRaw playerMove) {

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

}
