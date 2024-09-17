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

/**
 * 
 * @author Benoit Lacelle
 * @see RedisAutoConfiguration
 */
@Configuration
@Profile(IKumiteSpringProfiles.P_REDIS)
public class KumiteRedisConfiguration {

	// @Bean
	// public JedisConnectionFactory redisConnectionFactory() {
	// // https://app.redislabs.com/#/databases/12516930/subscription/2419490/view-bdb/configuration
	// RedisStandaloneConfiguration config =
	// new RedisStandaloneConfiguration("redis-10694.c242.eu-west-1-2.ec2.redns.redis-cloud.com", 10694);
	// return new JedisConnectionFactory(config);
	// }

	@Bean
	public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory,
			ObjectMapper objectMapper) {
		RedisTemplate<Object, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(redisConnectionFactory);

		// We prefer to rely on Jackson serialized than JDK Serialized, as Jackson will enable easier
		// forward/backward-compatibility
		template.setDefaultSerializer(GenericJackson2JsonRedisSerializer.builder()
				.objectMapper(objectMapper)
				// This will enable Redis to read back to the proper type
				.defaultTyping(true)
				.build());

		return template;
	}
}