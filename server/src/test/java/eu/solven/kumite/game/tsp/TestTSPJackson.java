package eu.solven.kumite.game.tsp;

import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.solven.kumite.app.KumiteJackson;
import eu.solven.kumite.board.IKumiteBoardView;
import eu.solven.kumite.game.optimization.tsp.TSPBoard;
import eu.solven.kumite.game.optimization.tsp.TSPCity;
import eu.solven.kumite.game.optimization.tsp.TSPProblem;

public class TestTSPJackson {
	ObjectMapper objectMapper = KumiteJackson.objectMapper();

	private TSPBoard makeBoard() {
		TSPProblem problem =
				TSPProblem.builder().city(TSPCity.builder().name("cityName").x(1.2).y(2.3).build()).build();
		TSPBoard board = TSPBoard.builder().problem(problem).build();
		return board;
	}

	@Test
	public void testBoard() throws JsonMappingException, JsonProcessingException {
		TSPBoard board = makeBoard();

		String asString = objectMapper.writeValueAsString(board);

		TSPBoard fromString = objectMapper.readValue(asString, TSPBoard.class);

		Assertions.assertThat(fromString).isEqualTo(board);
	}

	@Test
	public void testView() throws JsonMappingException, JsonProcessingException {
		TSPBoard board = makeBoard();

		IKumiteBoardView view = board.asView(UUID.randomUUID());

		String asString = objectMapper.writeValueAsString(view);

		TSPProblem fromString = objectMapper.readValue(asString, TSPProblem.class);

		Assertions.assertThat(fromString).isEqualTo(view);
	}
}
