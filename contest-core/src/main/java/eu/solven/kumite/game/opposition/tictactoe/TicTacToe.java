package eu.solven.kumite.game.opposition.tictactoe;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.random.RandomGenerator;

import eu.solven.kumite.board.IHasBoard;
import eu.solven.kumite.board.IKumiteBoard;
import eu.solven.kumite.board.IKumiteBoardView;
import eu.solven.kumite.contest.IHasGameover;
import eu.solven.kumite.game.GameMetadata;
import eu.solven.kumite.game.IGame;
import eu.solven.kumite.game.IGameMetadataConstants;
import eu.solven.kumite.leaderboard.Leaderboard;
import eu.solven.kumite.move.IKumiteMove;
import eu.solven.kumite.move.WaitForPlayersMove;
import eu.solven.kumite.move.WaitForSignups;

public class TicTacToe implements IGame {
	GameMetadata gameMetadata = GameMetadata.builder()
			.gameId(UUID.fromString("39cf37f9-09bd-402a-9d61-de63010d7354"))
			.title("Tic-Tac-Toe")
			.tag(IGameMetadataConstants.TAG_1V1)
			.tag(IGameMetadataConstants.TAG_PERFECT_INFORMATION)
			.tag(IGameMetadataConstants.TAG_TURNBASED)
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
	public Map<String, IKumiteMove> exampleMoves(RandomGenerator randomGenerator,
			IKumiteBoardView boardView,
			UUID playerId) {
		TicTacToeBoard board = (TicTacToeBoard) boardView;

		if (board.isGameOver()) {
			return Collections.singletonMap("Game is over", WaitForSignups.builder().build());
		}

		if (board.getPlayerIdToSymbol().size() != 2) {
			// There is no possible move until there is 2 players
			return Collections.singletonMap("Wait for sign-ups", WaitForSignups.builder().build());
		}

		char nextPlayerSymbol = board.getNextPlayerSymbol();

		char playerMoveSymbol = board.getPlayerSymbol(playerId);

		if (nextPlayerSymbol != playerMoveSymbol) {
			// Next turn is not for this player: no move is available
			Set<UUID> signedUpPlayers = new TreeSet<>(board.getPlayerIdToSymbol().keySet());
			signedUpPlayers.remove(playerId);
			if (signedUpPlayers.size() != 1) {
				throw new IllegalStateException(
						"Issue given playerId=" + playerId + " and " + board.getPlayerIdToSymbol());
			}
			UUID otherPlayerId = signedUpPlayers.iterator().next();
			WaitForPlayersMove waitForPlayerMove = WaitForPlayersMove.builder().waitedPlayer(otherPlayerId).build();
			return Collections.singletonMap("Wait for player `%s`".formatted(nextPlayerSymbol), waitForPlayerMove);
		}

		char[] positions = board.getPositions();

		Map<String, IKumiteMove> moves = new TreeMap<>();
		for (int i = 0; i < positions.length; i++) {
			if ('_' == positions[i]) {
				// This is a playable position
				int oneBasePosition = i + 1;
				moves.put(Integer.toString(oneBasePosition), TicTacToeMove.builder().position(oneBasePosition).build());
			} else {
				// This position is occupied
			}
		}

		return moves;
	}

	@Override
	public IHasGameover makeDynamicGameover(IHasBoard rawBoard) {

		// TODO Implement a timeout logic
		return () -> {
			TicTacToeBoard board = (TicTacToeBoard) rawBoard.get();
			return board.isGameOver();
		};
	}

	@Override
	public Leaderboard makeLeaderboard(IKumiteBoard rawBoard) {
		TicTacToeBoard board = (TicTacToeBoard) rawBoard;

		return board.makeLeaderboard();
	}
}
