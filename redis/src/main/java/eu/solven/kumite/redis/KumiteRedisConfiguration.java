package eu.solven.kumite.redis;

import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;

import eu.solven.kumite.app.IKumiteSpringProfiles;
import eu.solven.kumite.app.KumiteJackson;

/**
 * 
 * @author Benoit Lacelle
 * @see RedisAutoConfiguration
 */
@Configuration
@Profile(IKumiteSpringProfiles.P_REDIS)
public class KumiteRedisConfiguration {

	@Bean
	public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
		RedisTemplate<Object, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(redisConnectionFactory);

		// We make a dedicated objectMapper, as `defaultTyping(true)` will mutate it
		ObjectMapper redisObjectmapper = KumiteJackson.objectMapper();

		// We prefer to rely on Jackson serialized than JDK Serialized, as Jackson will enable easier
		// forward/backward-compatibility
		template.setDefaultSerializer(GenericJackson2JsonRedisSerializer.builder()
				.objectMapper(redisObjectmapper)
				// This will enable Redis to read back to the proper type
				.defaultTyping(true)
				.build());

		return template;
	}
}