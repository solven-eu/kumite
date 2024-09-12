package eu.solven.kumite.tsp;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.solven.kumite.game.optimization.tsp.TSPCity;
import eu.solven.kumite.game.optimization.tsp.TSPProblem;

public class TestTSPJackson {
	ObjectMapper objectMapper = new ObjectMapper();

	@Test
	public void testJackson() throws JsonMappingException, JsonProcessingException {
		TSPProblem p = TSPProblem.builder().city(TSPCity.builder().name("cityName").x(1.2).y(2.3).build()).build();

		String asString = objectMapper.writeValueAsString(p);

		TSPProblem fromString = objectMapper.readValue(asString, TSPProblem.class);

		Assertions.assertThat(fromString).isEqualTo(p);
	}
}
