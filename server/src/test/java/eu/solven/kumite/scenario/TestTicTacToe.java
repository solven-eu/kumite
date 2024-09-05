package eu.solven.kumite.scenario;

import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.solven.kumite.board.IKumiteBoard;
import eu.solven.kumite.game.opposition.tictactoe.TicTacToe;
import eu.solven.kumite.game.opposition.tictactoe.TicTacToeBoard;
import eu.solven.kumite.game.optimization.tsp.IKumiteBoardView;
import eu.solven.kumite.player.IKumiteMove;
import eu.solven.kumite.player.PlayerMoveRaw;

public class TestTicTacToe {
	ObjectMapper objectMapper = new ObjectMapper();

	@Test
	public void testGame() throws JsonMappingException, JsonProcessingException {
		TicTacToe game = new TicTacToe();

		IKumiteBoard board = game.generateInitialBoard(new Random(0));

		int nbMinPlayers = game.getGameMetadata().getMinPlayers();

		for (int iPlayer = 0; iPlayer < nbMinPlayers; iPlayer++) {
			UUID playerId = UUID.randomUUID();
			board.registerPlayer(playerId);

			IKumiteBoardView boardView = board.asView(playerId);
			{
				String boardAsString = objectMapper.writeValueAsString(boardView);
				IKumiteBoardView fromString = objectMapper.readValue(boardAsString, TicTacToeBoard.class);
				Assertions.assertThat(fromString).isEqualTo(boardView);
			}

			Map<String, IKumiteMove> exampleMoves = game.exampleMoves(boardView, playerId);

			exampleMoves.values().forEach(exampleMove -> {
				PlayerMoveRaw playerMove = PlayerMoveRaw.builder().playerId(playerId).move(exampleMove).build();
				boardView.isValidMove(playerMove);
			});

			if (!exampleMoves.isEmpty()) {
				PlayerMoveRaw playerMove = PlayerMoveRaw.builder()
						.playerId(playerId)
						.move(exampleMoves.values().iterator().next())
						.build();
				board.registerMove(playerMove);
			}

		}
	}
}
