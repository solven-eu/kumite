package eu.solven.kumite.scenario;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.solven.kumite.board.IKumiteBoard;
import eu.solven.kumite.board.IKumiteBoardView;
import eu.solven.kumite.contest.IHasGameover;
import eu.solven.kumite.game.opposition.tictactoe.TicTacToe;
import eu.solven.kumite.game.opposition.tictactoe.TicTacToeBoard;
import eu.solven.kumite.game.opposition.tictactoe.TicTacToeMove;
import eu.solven.kumite.player.IKumiteMove;
import eu.solven.kumite.player.PlayerMoveRaw;

public class TestTicTacToe {
	ObjectMapper objectMapper = new ObjectMapper();

	@Test
	public void testGame() throws JsonMappingException, JsonProcessingException {
		TicTacToe game = new TicTacToe();

		IKumiteBoard board = game.generateInitialBoard(new Random(0));

		int nbMinPlayers = game.getGameMetadata().getMinPlayers();

		Map<Integer, UUID> playerIndexToPlayerId = new HashMap<>();

		IHasGameover hasGameover = game.makeDynamicGameover(() -> board);

		// Let all players plays one after the others
		// 1 move for registration, and 5 moves filling the board
		for (int moveIndex = 0; moveIndex < 1 + 5; moveIndex++) {
			for (int iPlayer = 0; iPlayer < nbMinPlayers; iPlayer++) {
				UUID playerId = playerIndexToPlayerId.computeIfAbsent(iPlayer, k -> UUID.randomUUID());

				if (moveIndex == 0) {
					// The first move is to register each player
					board.registerContender(playerId);
				}

				IKumiteBoardView boardView = board.asView(playerId);
				{
					String boardAsString = objectMapper.writeValueAsString(boardView);
					IKumiteBoardView fromString = objectMapper.readValue(boardAsString, TicTacToeBoard.class);
					Assertions.assertThat(fromString).isEqualTo(boardView);
				}

				Map<String, IKumiteMove> exampleMoves = game.exampleMoves(boardView, playerId);

				List<IKumiteMove> playableMoves = exampleMoves.values()
						.stream()
						// Filter cases like WaitForSignupsMove
						.filter(m -> m instanceof TicTacToeMove)
						.collect(Collectors.toList());
				playableMoves.forEach(exampleMove -> {
					PlayerMoveRaw playerMove = PlayerMoveRaw.builder().playerId(playerId).move(exampleMove).build();
					boardView.isValidMove(playerMove);
				});

				if (!playableMoves.isEmpty()) {
					PlayerMoveRaw playerMove = PlayerMoveRaw.builder()
							.playerId(playerId)
							.move(exampleMoves.values().iterator().next())
							.build();
					board.registerMove(playerMove);
				}
			}

			if (moveIndex < 3) {
				Assertions.assertThat(hasGameover.isGameOver()).isFalse();
			}

		}

		// There must be a winner after having played at most 5 pair of moves
		Assertions.assertThat(hasGameover.isGameOver()).isTrue();
	}
}
