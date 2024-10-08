package eu.solven.kumite.account;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.solven.kumite.account.internal.KumiteUser;
import eu.solven.kumite.account.login.IKumiteTestConstants;
import eu.solven.kumite.app.KumiteJackson;

public class TestKumiteUser implements IKumiteTestConstants {
	final ObjectMapper objectMapper = KumiteJackson.objectMapper();

	@Test
	public void testJackson() throws JsonMappingException, JsonProcessingException {
		KumiteUser initial = KumiteUser.builder()
				.accountId(someAccountId)
				.rawRaw(IKumiteTestConstants.userRawRaw())
				.details(IKumiteTestConstants.userDetails())
				.playerId(somePlayerId)
				.build();

		String asString = objectMapper.writeValueAsString(initial);

		// Assertions.assertThat(asString).isEqualTo("");

		// Assertions.assertThatThrownBy(() -> objectMapper.readValue(asString, KumiteUser.class))
		// .isInstanceOf(InvalidDefinitionException.class);

		KumiteUser fromString = objectMapper.readValue(asString, KumiteUser.class);

		Assertions.assertThat(fromString).isEqualTo(initial);
	}

}
