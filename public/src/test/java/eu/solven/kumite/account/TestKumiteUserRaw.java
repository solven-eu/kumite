package eu.solven.kumite.account;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.solven.kumite.account.internal.KumiteUserRaw;
import eu.solven.kumite.account.login.IKumiteTestConstants;
import eu.solven.kumite.app.KumiteJackson;

public class TestKumiteUserRaw implements IKumiteTestConstants {
	final ObjectMapper objectMapper = KumiteJackson.objectMapper();

	@Test
	public void testJackson() throws JsonMappingException, JsonProcessingException {
		KumiteUserDetails userRaw = IKumiteTestConstants.userDetails();
		KumiteUserRaw initial =
				KumiteUserRaw.builder().accountId(someAccountId).playerId(somePlayerId).details(userRaw).build();

		String asString = objectMapper.writeValueAsString(initial);

		KumiteUserRaw fromString = objectMapper.readValue(asString, KumiteUserRaw.class);

		Assertions.assertThat(fromString).isEqualTo(initial);
	}

}
