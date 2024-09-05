package eu.solven.kumite.game.opposition.tictactoe;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.random.RandomGenerator;

import eu.solven.kumite.board.IKumiteBoard;
import eu.solven.kumite.game.GameMetadata;
import eu.solven.kumite.game.IGame;
import eu.solven.kumite.game.IGameMetadataConstants;
import eu.solven.kumite.game.optimization.tsp.IKumiteBoardView;
import eu.solven.kumite.player.IKumiteMove;

public class TicTacToe implements IGame {
	GameMetadata gameMetadata = GameMetadata.builder()
			.gameId(UUID.fromString("39cf37f9-09bd-402a-9d61-de63010d7354"))
			.title("Tic-Tac-Toe")
			.tag(IGameMetadataConstants.TAG_1V1)
			.tag(IGameMetadataConstants.TAG_PERFECT_INFORMATION)
			.minPlayers(2)
			.maxPlayers(2)
			.shortDescription(
					"The player who succeeds in placing three of their marks in a horizontal, vertical, or diagonal row is the winner.")
			.reference(URI.create("https://en.wikipedia.org/wiki/Tic-tac-toe"))
			.build();

	@Override
	public GameMetadata getGameMetadata() {
		return gameMetadata;
	}

	@Override
	public IKumiteBoard generateInitialBoard(RandomGenerator random) {
		return TicTacToeBoard.builder().build();
	}

	@Override
	public IKumiteMove parseRawMove(Map<String, ?> rawMove) {
		Object rawPosition = rawMove.get("position");
		if (rawPosition instanceof Number position) {
			int positionAsInt = position.intValue();

			if (positionAsInt < 1 || positionAsInt > 10) {
				throw new IllegalArgumentException("Invalid position: " + positionAsInt);
			}

			return TicTacToeMove.builder().position(positionAsInt).build();
		}

		throw new IllegalArgumentException("Invalid position: " + rawMove);
	}

	@Override
	public IKumiteBoard parseRawBoard(Map<String, ?> rawBoard) {
		return TicTacToeBoard.builder().build();
	}

	@Override
	public Map<String, IKumiteMove> exampleMoves(IKumiteBoardView boardView, UUID playerId) {

		TicTacToeBoard board = (TicTacToeBoard) boardView;

		char nextPlayerSymbol = board.getNextPlayerSymbol();

		char playerMoveSymbol = board.getPlayerSymbol(playerId);

		if (nextPlayerSymbol != playerMoveSymbol) {
			// Next turn is not for this player: no move is available
			return Collections.emptyMap();
		}

		char[] positions = board.getPositions();

		Map<String, IKumiteMove> moves = new TreeMap<>();
		for (int i = 0; i < positions.length; i++) {
			moves.put(Character.toString(positions[i]), TicTacToeMove.builder().position(i + 1).build());
		}

		return moves;
	}

}
