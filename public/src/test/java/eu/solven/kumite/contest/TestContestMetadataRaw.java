package eu.solven.kumite.contest;

import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.solven.kumite.app.KumiteJackson;

public class TestContestMetadataRaw {
	final ObjectMapper objectMapper = KumiteJackson.objectMapper();

	@Test
	public void testJacksonWaitForPlayers() throws JsonMappingException, JsonProcessingException {
		ContestMetadataRaw initial = ContestMetadataRaw.builder()
				.contestId(UUID.randomUUID())
				.constantMetadata(ContestCreationMetadata.empty())
				.dynamicMetadata(ContestDynamicMetadata.builder().build())
				.build();

		String asString = objectMapper.writeValueAsString(initial);

		// Ensure we write timestamp as ISO:
		{

			String createdAsString = initial.getConstantMetadata().getCreated().toString();

			Assertions.assertThat(createdAsString).endsWith("Z");
			createdAsString = createdAsString.substring(0, createdAsString.length() - "Z".length());

			// Jackson would strip the trailing 0 in milliseconds
			while (createdAsString.endsWith("0")) {
				// Jackson strips trailing 0 in milliseconds
				createdAsString = createdAsString.substring(0, createdAsString.length() - 1);
			}

			Assertions.assertThat(asString).contains(createdAsString + "Z");
		}

		ContestMetadataRaw fromString = objectMapper.readValue(asString, ContestMetadataRaw.class);

		Assertions.assertThat(fromString).isEqualTo(initial);
	}

}
