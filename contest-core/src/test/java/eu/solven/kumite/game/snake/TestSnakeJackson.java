package eu.solven.kumite.game.snake;

import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.solven.kumite.app.KumiteJackson;
import eu.solven.kumite.board.IKumiteBoardView;

public class TestSnakeJackson {
	ObjectMapper objectMapper = KumiteJackson.objectMapper();

	private SnakeBoard makeBoard() {
		SnakeBoard board = SnakeBoard.builder().build();
		return board;
	}

	@Test
	public void testBoard() throws JsonMappingException, JsonProcessingException {
		SnakeBoard board = makeBoard();

		String asString = objectMapper.writeValueAsString(board);

		SnakeBoard fromString = objectMapper.readValue(asString, SnakeBoard.class);

		Assertions.assertThat(fromString).isEqualTo(board);
	}

	@Test
	public void testView() throws JsonMappingException, JsonProcessingException {
		SnakeBoard board = makeBoard();
		IKumiteBoardView view = board.asView(UUID.randomUUID());

		String asString = objectMapper.writeValueAsString(view);

		SnakeBoard fromString = objectMapper.readValue(asString, SnakeBoard.class);

		Assertions.assertThat(fromString).isEqualTo(view);
	}
}
