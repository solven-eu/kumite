package eu.solven.kumite.game.lag;

import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.solven.kumite.app.KumiteJackson;
import eu.solven.kumite.board.IKumiteBoardView;
import eu.solven.kumite.game.optimization.lag.LagBoard;

public class TestLagJackson {
	ObjectMapper objectMapper = KumiteJackson.objectMapper();

	private LagBoard makeBoard() {
		LagBoard board = LagBoard.builder().build();
		return board;
	}

	@Test
	public void testBoard() throws JsonMappingException, JsonProcessingException {
		LagBoard board = makeBoard();

		String asString = objectMapper.writeValueAsString(board);

		LagBoard fromString = objectMapper.readValue(asString, LagBoard.class);

		Assertions.assertThat(fromString).isEqualTo(board);
	}

	@Test
	public void testView() throws JsonMappingException, JsonProcessingException {
		LagBoard board = makeBoard();
		IKumiteBoardView view = board.asView(UUID.randomUUID());

		String asString = objectMapper.writeValueAsString(view);

		LagBoard fromString = objectMapper.readValue(asString, LagBoard.class);

		Assertions.assertThat(fromString).isEqualTo(view);
	}
}
