package eu.solven.kumite.game.tsp;

import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.solven.kumite.move.WaitForPlayersMove;
import eu.solven.kumite.move.WaitForSignups;

public class TestWaitMovesJackson {
	final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	public void testJacksonWaitForPlayers() throws JsonMappingException, JsonProcessingException {
		WaitForPlayersMove p = WaitForPlayersMove.builder().waitedPlayer(UUID.randomUUID()).build();

		String asString = objectMapper.writeValueAsString(p);
		Assertions.assertThat(asString).contains("\"wait\":true");

		WaitForPlayersMove fromString = objectMapper.readValue(asString, WaitForPlayersMove.class);

		Assertions.assertThat(fromString).isEqualTo(p);
	}

	@Test
	public void testJacksonWaitForSignups() throws JsonMappingException, JsonProcessingException {
		WaitForSignups p = WaitForSignups.builder().build();

		String asString = objectMapper.writeValueAsString(p);
		Assertions.assertThat(asString).contains("\"wait\":true");

		WaitForSignups fromString = objectMapper.readValue(asString, WaitForSignups.class);

		Assertions.assertThat(fromString).isEqualTo(p);
	}
}
