package eu.solven.kumite.account;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.solven.kumite.account.login.IKumiteTestConstants;
import eu.solven.kumite.app.KumiteJackson;

public class TestKumiteUser implements IKumiteTestConstants {
	final ObjectMapper objectMapper = KumiteJackson.objectMapper();

	@Test
	public void testJackson() throws JsonMappingException, JsonProcessingException {
		KumiteUserRaw userRaw = IKumiteTestConstants.userRaw();
		KumiteUser initial = KumiteUser.builder().accountId(someAccountId).playerId(somePlayerId).raw(userRaw).build();

		String asString = objectMapper.writeValueAsString(initial);

		KumiteUser fromString = objectMapper.readValue(asString, KumiteUser.class);

		Assertions.assertThat(fromString).isEqualTo(initial);
	}

}
