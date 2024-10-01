package eu.solven.kumite.redis;

import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.solven.kumite.account.KumiteUserDetails;
import eu.solven.kumite.account.KumiteUserRawRaw;
import eu.solven.kumite.account.KumiteUsersRegistry;
import eu.solven.kumite.account.internal.KumiteUser;
import eu.solven.kumite.account.internal.KumiteUserPreRegister;
import eu.solven.kumite.app.IKumiteSpringProfiles;
import eu.solven.kumite.app.KumiteJackson;
import eu.solven.kumite.app.KumiteServerComponentsConfiguration;
import eu.solven.kumite.app.persistence.RedisKumiteConfiguration;
import lombok.extern.slf4j.Slf4j;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {

		KumiteServerComponentsConfiguration.class,

		RedisKumiteConfiguration.class,

		TestSpringAutonomyRedis.Complement.class,

}, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles({ IKumiteSpringProfiles.P_REDIS })
@TestPropertySource(properties = {
		// https://github.com/spring-projects/spring-boot/issues/4877
		"rediscloud.url" + "=redis://localhost:6379",
		"spring.data.redis.host" + "=localhost",
		"spring.data.redis.port" + "=6379", })
@Import({ RedisTestConfiguration.class })
@Slf4j
@ImportAutoConfiguration({

		RedisAutoConfiguration.class,

})
public class TestSpringAutonomyRedis implements IKumiteSpringProfiles {

	public static class Complement {
		@Bean
		ObjectMapper objectMapper() {
			return KumiteJackson.objectMapper();
		}
	}

	@Autowired
	ApplicationContext appContext;

	@Autowired
	ObjectMapper objectMapper;

	@Test
	public void testSpringProfiles() {
		log.info("startupDate: {}", appContext.getStartupDate());
	}

	@Test
	public void testObjectMapper() {
		// Ensure Redis did not corrupt the ObjectMapper Bean with its own configuration
		KumiteUserRawRaw rawRaw = KumiteUserRawRaw.builder().providerId("someProviderId").sub("someSub").build();
		@SuppressWarnings("rawtypes")
		Map asMap = objectMapper.convertValue(rawRaw, Map.class);
		Assertions.assertThat(asMap).hasSize(2);
	}

	@Test
	public void updateUserRaw() {
		KumiteUsersRegistry usersRegistry = appContext.getBean(KumiteUsersRegistry.class);

		KumiteUserRawRaw rawRaw = KumiteUserRawRaw.builder().providerId("test").sub("test").build();

		KumiteUserPreRegister rawInitial = KumiteUserPreRegister.builder()
				.rawRaw(rawRaw)
				.details(KumiteUserDetails.builder().username("testUsername").company("someCompany").build())
				.build();
		usersRegistry.registerOrUpdate(rawInitial);

		KumiteUserPreRegister rawLater = KumiteUserPreRegister.builder()
				.rawRaw(rawRaw)
				.details(KumiteUserDetails.builder().username("testUsername").countryCode("someCountryCode").build())
				.build();
		KumiteUser finalUser = usersRegistry.registerOrUpdate(rawLater);

		Assertions.assertThat(finalUser.getDetails().getCompany()).isEqualTo("someCompany");
		Assertions.assertThat(finalUser.getDetails().getCountryCode()).isEqualTo("someCountryCode");
	}
}
